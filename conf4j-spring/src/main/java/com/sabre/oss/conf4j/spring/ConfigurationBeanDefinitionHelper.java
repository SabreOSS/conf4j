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

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;

import static com.sabre.oss.conf4j.spring.ConfigurationBeanDefinitionHelper.ConfigurationIndicator.ABSENT;

/**
 * Utility class with set of methods simplifying indication if bean definition is conf4j configuration.
 */
public final class ConfigurationBeanDefinitionHelper {
    /**
     * Specifies the name of an optional, synthetic attribute added to the bean definition to indicate
     * it has been found by scanner.
     */
    public static final String CONF4J_CONFIGURATION_INDICATOR = "$conf4jConfigurationIndicator";

    private ConfigurationBeanDefinitionHelper() {
    }

    /**
     * Configuration indicator specifies what was the source of registration.
     */
    public enum ConfigurationIndicator {
        /**
         * Indicates the bean is not marked with an indicator.
         */
        ABSENT,
        /**
         * Bean has been discovered automatically and there is a possibility it is not a valid configuration type
         * (configuration scanner does not have access to all metadata).
         */
        DISCOVERED,
        /**
         * Bean has been registered manually. In case it will be recognized as an invalid configuration type,
         * an error will be reported.
         */
        MANUAL,
    }

    /**
     * Add to the bean definition an indicator which says the definition is a conf4j configuration.
     *
     * @param beanDefinition         bean definition
     * @param configurationIndicator configuration indicator
     * @throws NullPointerException when {@code beanDefinition} is {@code null}.
     */
    public static void addConf4jConfigurationIndicator(BeanDefinition beanDefinition, ConfigurationIndicator configurationIndicator) {
        PropertyValue conf4jConfigurationIndicator = new PropertyValue(CONF4J_CONFIGURATION_INDICATOR, configurationIndicator);
        conf4jConfigurationIndicator.setOptional(true);
        beanDefinition.getPropertyValues().addPropertyValue(conf4jConfigurationIndicator);
    }

    /**
     * Ge if bean definition is a conf4j configuration.
     *
     * @param beanDefinition bean definition
     * @return configuration indicator (never {@code null}).
     * @throws NullPointerException when {@code beanDefinition} is {@code null}.
     */
    public static ConfigurationIndicator getConf4jConfigurationIndicator(BeanDefinition beanDefinition) {
        Object indicator = beanDefinition.getPropertyValues().get(CONF4J_CONFIGURATION_INDICATOR);
        return indicator == null ? ABSENT : (ConfigurationIndicator) indicator;
    }
}
