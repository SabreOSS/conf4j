/*
 * MIT License
 *
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

package com.sabre.oss.conf4j.spring.source;

import com.sabre.oss.conf4j.source.ConfigurationEntry;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import com.sabre.oss.conf4j.source.OptionalValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.*;

import java.util.*;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static org.springframework.context.ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME;

/**
 * Configuration source which gets the configuration value from either
 * {@linkplain #setPropertySources(PropertySources) propertySources}
 * or flattenedPropertySources {@link PropertySourcesPlaceholderConfigurer} registered in the context
 * (when {@linkplain #setPropertySources(PropertySources) propertySources} is not set)
 * or from {@link Environment} (when {@linkplain #setPropertySources(PropertySources) propertySources} is not and
 * there is no {@link PropertySourcesPlaceholderConfigurer} registered).
 * <p>
 * Usually only one instance of this source should be registered in the context.
 * </p>
 */
public class PropertySourceConfigurationSource implements ConfigurationSource, BeanFactoryAware, EnvironmentAware, InitializingBean {
    private List<PropertySource<?>> flattenedPropertySources;
    private PropertySources propertySources;
    private ConversionService conversionService;
    private DefaultConversionService defaultConversionService;
    private BeanFactory beanFactory;
    private Environment environment = new StandardEnvironment();

    /**
     * @param propertySources property sources
     */
    public void setPropertySources(PropertySources propertySources) {
        this.propertySources = propertySources;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * @param conversionService the conversionService to set
     */
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.propertySources == null) {
            this.flattenedPropertySources = deducePropertySources();
        } else {
            this.flattenedPropertySources = singletonList((PropertySource<?>) propertySources);
        }

        if (this.conversionService == null) {
            this.conversionService = getOptionalBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
        }
    }

    @Override
    public OptionalValue<String> getValue(String key, Map<String, String> attributes) {
        requireNonNull(key, "key cannot be null");

        for (PropertySource<?> propertySource : flattenedPropertySources) {
            OptionalValue<String> value = getProperty(propertySource, key);
            if (value.isPresent()) {
                return value;
            }
        }
        return absent();
    }

    @Override
    public ConfigurationEntry findEntry(Collection<String> keys, Map<String, String> attributes) {
        requireNonNull(keys, "keys cannot be null");

        for (PropertySource<?> propertySource : flattenedPropertySources) {
            for (String key : keys) {
                OptionalValue<String> value = getProperty(propertySource, key);
                if (value.isPresent()) {
                    return new ConfigurationEntry(key, value.get());
                }
            }
        }
        return null;
    }

    private OptionalValue<String> getProperty(PropertySource<?> propertySource, String key) {
        Object value = propertySource.getProperty(key);
        if (value == null) {
            return propertySource.containsProperty(key) ? present(null) : absent();
        }

        ConversionService currentConversionService = (this.conversionService != null) ? this.conversionService : getDefaultConversionService();
        String stringValue = currentConversionService.convert(value, String.class);

        return present(stringValue);
    }

    private List<PropertySource<?>> deducePropertySources() {
        List<PropertySourcesPlaceholderConfigurer> configurers = getAllPropertySourcesPlaceholderConfigurers();
        if (!configurers.isEmpty()) {
            // PropertySource equals() method is based on name, but Spring registers some property sources
            // with fixed name e.g. PropertySourcesPlaceholderConfigurer.LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME.
            Map<PropertySource<?>, PropertySource<?>> propertySourcesMap = new IdentityHashMap<>();
            List<PropertySource<?>> allPropertySources = new ArrayList<>();
            for (PropertySourcesPlaceholderConfigurer configurer : configurers) {
                for (PropertySource<?> propertySource : configurer.getAppliedPropertySources()) {
                    if (propertySourcesMap.put(propertySource, propertySource) == null) {
                        allPropertySources.add(propertySource);
                    }
                }
            }

            return flattenPropertySources(allPropertySources);
        }

        if (this.environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) this.environment;
            List<PropertySource<?>> allPropertySources = new ArrayList<>(configurableEnvironment.getPropertySources().size());
            for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
                allPropertySources.add(propertySource);
            }

            return flattenPropertySources(allPropertySources);
        }

        return emptyList();
    }

    private List<PropertySource<?>> flattenPropertySources(List<PropertySource<?>> propertySources) {
        List<PropertySource<?>> result = new ArrayList<>();
        for (PropertySource<?> propertySource : propertySources) {
            flattenPropertySources(propertySource, result);
        }
        return result;
    }

    private void flattenPropertySources(PropertySource<?> propertySource, List<PropertySource<?>> result) {
        Object source = propertySource.getSource();
        if (source instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) source;
            for (PropertySource<?> childSource : configurableEnvironment.getPropertySources()) {
                flattenPropertySources(childSource, result);
            }
        } else {
            result.add(propertySource);
        }
    }

    private List<PropertySourcesPlaceholderConfigurer> getAllPropertySourcesPlaceholderConfigurers() {
        if (!(this.beanFactory instanceof ListableBeanFactory)) {
            return emptyList();
        }

        ListableBeanFactory listableBeanFactory = (ListableBeanFactory) this.beanFactory;
        // take care not to cause early instantiation of flattenedPropertySources FactoryBeans
        Map<String, PropertySourcesPlaceholderConfigurer> beans = listableBeanFactory
                .getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false, false);
        List<PropertySourcesPlaceholderConfigurer> configurers = new ArrayList<>(beans.values());
        configurers.sort(comparingInt(PropertyResourceConfigurer::getOrder));

        return configurers;
    }

    private ConversionService getDefaultConversionService() {
        if (this.defaultConversionService == null) {
            DefaultConversionService cs = new DefaultConversionService();
            for (Converter<?, ?> converter : ((ListableBeanFactory) this.beanFactory).getBeansOfType(Converter.class, false, false).values()) {
                cs.addConverter(converter);
            }
            this.defaultConversionService = cs;
        }
        return this.defaultConversionService;
    }

    private <T> T getOptionalBean(String name, Class<T> type) {
        try {
            return this.beanFactory.getBean(name, type);
        } catch (NoSuchBeanDefinitionException ignore) {
            return null;
        }
    }
}

