/*
 * Copyright 2017 Sabre GLBL Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.sabre.oss.conf4j.spring;

import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.internal.model.ConfigurationModelProvider;
import com.sabre.oss.conf4j.spring.ConfigurationBeanDefinitionHelper.ConfigurationIndicator;
import com.sabre.oss.conf4j.spring.annotation.ConfigurationScan;
import com.sabre.oss.conf4j.spring.annotation.ConfigurationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.Set;

import static com.sabre.oss.conf4j.spring.Conf4jSpringConstants.CONF4J_CONFIGURATION_FACTORY;
import static com.sabre.oss.conf4j.spring.Conf4jSpringConstants.CONF4J_CONFIGURATION_VALUES_SOURCE;
import static com.sabre.oss.conf4j.spring.ConfigurationBeanDefinitionHelper.getConf4jConfigurationIndicator;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * Replaces conf4j configuration abstract classes discovered by {@link ConfigurationScan}, {@link ConfigurationType}
 * {@code <conf4j:configuration-scan .../>} or {@code <conf4j:configuration .../>} or directly registered in context and annotated
 * with {@link Component} (or any other annotation meta-annotated by this annotation)
 * with the actual configuration instance generated by the configuration factory.
 * <p>
 * Annotation which is used for detecting configurations can be customized by {@link #setConfigurationAnnotations(Set)}
 * and by default is {@link Component}.
 * </p>
 */
public class ConfigurationBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, BeanClassLoaderAware {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationBeanFactoryPostProcessor.class);

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
    private Set<Class<? extends Annotation>> configurationAnnotations = singleton(Component.class);
    private int order = Ordered.HIGHEST_PRECEDENCE;
    private ConfigurationModelProvider configurationModelProvider;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    /**
     * Set of configuration annotations which are used for detecting whether a class is a certain configuration type.
     *
     * @param configurationAnnotations set of annotations.
     */
    public void setConfigurationAnnotations(Set<Class<? extends Annotation>> configurationAnnotations) {
        this.configurationAnnotations = requireNonNull(configurationAnnotations, "configurationAnnotations cannot be null");
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder() {
        this.order = order;
    }

    /**
     * Sets {@link ConfigurationModelProvider} which is used for determining a class is a conf4j configuration.
     * It must be the same as the model provider configured in {@link ConfigurationFactory} used
     * for creating configuration instances.
     *
     * @param configurationModelProvider configuration model provider.
     */
    public void setConfigurationModelProvider(ConfigurationModelProvider configurationModelProvider) {
        this.configurationModelProvider = configurationModelProvider;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        long start = nanoTime();
        int foundConfigurations = 0;

        for (String name : registry.getBeanDefinitionNames()) {
            BeanDefinition definition = registry.getBeanDefinition(name);

            /* Skips abstract beans */
            if (definition.isAbstract()) {
                continue;
            }

            String beanClassName = definition.getBeanClassName();
            /* Skip beans created by factories */
            if (beanClassName == null) {
                continue;
            }

            Class<?> configurationType;
            try {
                configurationType = ClassUtils.forName(beanClassName, this.beanClassLoader);
            } catch (ClassNotFoundException ignore) {
                continue;
            }

            if (isConfigurationType(configurationType)) {
                ConfigurationIndicator indicator = getConf4jConfigurationIndicator(definition);
                if (indicator != ConfigurationIndicator.ABSENT) {
                    log.trace("conf4j configuration bean {} of type {} found", name, beanClassName);
                    replaceBeanWithInstrumentedClass(definition, configurationType);
                    foundConfigurations++;
                }
            } else {
                ConfigurationIndicator indicator = getConf4jConfigurationIndicator(definition);
                if (indicator == ConfigurationIndicator.DISCOVERED) {
                    // Configuration type has been discovered so it is a possible class, which is not a valid configuration
                    // was registered unintentionally and it is safe to remove it.
                    for (String alias : registry.getAliases(name)) {
                        registry.removeAlias(alias);
                    }
                    registry.removeBeanDefinition(name);
                    log.warn("conf4j configuration bean {} of type {} is not recognized as configuration type, the bean definition has been removed.", name, beanClassName);
                } else if (indicator == ConfigurationIndicator.MANUAL) {
                    throw new BeanDefinitionValidationException(
                            format("conf4j configuration bean %s of type %s is not recognized as configuration type, but it was registered explicitly.",
                                    name, beanClassName));
                }
            }
        }

        log.debug("conf4j configurations post-processing completed, {} configurations found, total time {} ms",
                foundConfigurations, NANOSECONDS.toMillis(nanoTime() - start));

    }

    private boolean isConfigurationType(Class<?> configurationType) {
        if (configurationModelProvider != null) {
            return configurationModelProvider.isConfigurationType(configurationType);
        } else {
            // fallback to the simplified logic
            StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(configurationType);
            return metadata.isAbstract() && metadata.isIndependent() &&
                    configurationAnnotations.stream().anyMatch(a -> findAnnotation(configurationType, a) != null);
        }
    }

    private void replaceBeanWithInstrumentedClass(BeanDefinition definition, Class<?> configurationType) {
        definition.setBeanClassName(ConfigurationFactoryBean.class.getName());
        MutablePropertyValues propertyValues = definition.getPropertyValues();
        propertyValues.addPropertyValue("configurationFactory", new RuntimeBeanReference(CONF4J_CONFIGURATION_FACTORY));
        propertyValues.addPropertyValue("configurationValuesSource", new RuntimeBeanReference(CONF4J_CONFIGURATION_VALUES_SOURCE));
        propertyValues.addPropertyValue("configurationType", configurationType);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}
