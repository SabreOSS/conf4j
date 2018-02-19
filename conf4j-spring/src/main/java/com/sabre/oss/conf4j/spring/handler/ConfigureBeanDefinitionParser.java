/*
 * MIT License
 *
 * Copyright 2017-2018 Sabre GLBL Inc.
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

package com.sabre.oss.conf4j.spring.handler;

import com.sabre.oss.conf4j.factory.javassist.JavassistDynamicConfigurationFactory;
import com.sabre.oss.conf4j.internal.model.provider.convention.ConventionConfigurationModelProvider;
import com.sabre.oss.conf4j.spring.ConfigurationBeanFactoryPostProcessor;
import com.sabre.oss.conf4j.spring.converter.AggregatedConverter;
import com.sabre.oss.conf4j.spring.factory.cglib.CglibDynamicConfigurationFactory;
import com.sabre.oss.conf4j.spring.source.PropertySourceConfigurationSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import static com.sabre.oss.conf4j.spring.Conf4jSpringConstants.*;
import static java.lang.String.format;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

public class ConfigureBeanDefinitionParser extends AbstractBeanDefinitionParser {
    private static final String CONFIGURATION_FACTORY_ATTRIBUTE = "configuration-factory";

    private static final boolean isJavassistConfigurationFactoryAvailable = ClassUtils.isPresent(
            "com.sabre.oss.conf4j.factory.javassist.JavassistDynamicConfigurationFactory",
            ConfigureBeanDefinitionParser.class.getClassLoader());

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        if (!registry.containsBeanDefinition(CONF4J_TYPE_CONVERTER)) {
            BeanDefinitionBuilder builder = genericBeanDefinition(AggregatedConverter.class)
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                    .setInitMethodName("initialize")
                    .setAutowireMode(AUTOWIRE_BY_TYPE)
                    .setLazyInit(true);
            registry.registerBeanDefinition(CONF4J_TYPE_CONVERTER, builder.getBeanDefinition());
        }

        if (!registry.containsBeanDefinition(CONF4J_CONFIGURATION_MODEL_PROVIDER)) {
            BeanDefinitionBuilder builder = genericBeanDefinition(ConventionConfigurationModelProvider.class)
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                    .setLazyInit(true)
                    .setFactoryMethod("getInstance");
            registry.registerBeanDefinition(CONF4J_CONFIGURATION_MODEL_PROVIDER, builder.getBeanDefinition());
        }

        String configurationFactory = element.getAttribute(CONFIGURATION_FACTORY_ATTRIBUTE);
        if (!registry.containsBeanDefinition(CONF4J_CONFIGURATION_FACTORY)) {
            if (StringUtils.isEmpty(configurationFactory)) {
                // register JavassistDynamicConfigurationFactory or if it is not available fallback to CglibDynamicConfigurationFactory.
                configurationFactory = isJavassistConfigurationFactoryAvailable ?
                        JavassistDynamicConfigurationFactory.class.getName() :
                        CglibDynamicConfigurationFactory.class.getName();
            }
            BeanDefinitionBuilder builder = genericBeanDefinition(configurationFactory)
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                    .setLazyInit(true)
                    .addPropertyReference("typeConverter", CONF4J_TYPE_CONVERTER);

            if (registry.containsBeanDefinition(CONF4J_CONFIGURATION_MODEL_PROVIDER)) {
                builder.addPropertyReference("configurationModelProvider", CONF4J_CONFIGURATION_MODEL_PROVIDER);
            }
            if (registry.containsBeanDefinition(CONF4J_CONFIGURATION_VALUE_PROCESSORS)) {
                builder.addPropertyReference("configurationValueProcessors", CONF4J_CONFIGURATION_VALUE_PROCESSORS);
            }

            registry.registerBeanDefinition(CONF4J_CONFIGURATION_FACTORY, builder.getBeanDefinition());
        } else {
            BeanDefinition beanDefinition = registry.getBeanDefinition(CONF4J_CONFIGURATION_FACTORY);
            if (!StringUtils.isEmpty(configurationFactory) && !configurationFactory.equals(beanDefinition.getBeanClassName())) {
                throw new IllegalArgumentException(format("Configuration factory bean %s is registered in the context already" +
                                "and its class name is %s. Cannot register custom factory %s.",
                        CONF4J_CONFIGURATION_FACTORY, beanDefinition.getBeanClassName(), configurationFactory));
            }
        }

        if (!registry.containsBeanDefinition(CONF4J_CONFIGURATION_SOURCE)) {
            BeanDefinitionBuilder builder = genericBeanDefinition(PropertySourceConfigurationSource.class)
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                    .setLazyInit(true);

            registry.registerBeanDefinition(CONF4J_CONFIGURATION_SOURCE, builder.getBeanDefinition());
        }

        if (!registry.containsBeanDefinition(CONF4J_BEAN_FACTORY_POST_PROCESSOR)) {
            BeanDefinitionBuilder builder = genericBeanDefinition(ConfigurationBeanFactoryPostProcessor.class)
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                    .setLazyInit(true);
            builder.getRawBeanDefinition().setSynthetic(true);
            if (registry.containsBeanDefinition(CONF4J_CONFIGURATION_MODEL_PROVIDER)) {
                builder.addPropertyReference("configurationModelProvider", CONF4J_CONFIGURATION_MODEL_PROVIDER);
            }
            registry.registerBeanDefinition(CONF4J_BEAN_FACTORY_POST_PROCESSOR, builder.getBeanDefinition());
        }

        return null;
    }
}
