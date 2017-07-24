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

package com.sabre.oss.conf4j.internal.factory.jdkproxy;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.config.ConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.factory.AbstractDynamicConfigurationInitializer;
import com.sabre.oss.conf4j.internal.factory.ConfigurationInitializer;
import com.sabre.oss.conf4j.internal.factory.ConfigurationInstanceCreator;
import com.sabre.oss.conf4j.internal.factory.ConfigurationPropertiesAccessor;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.utils.KeyGenerator;
import com.sabre.oss.conf4j.source.Attributes;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;

import java.lang.reflect.Proxy;
import java.util.Map;

public class JdkProxyDynamicConfigurationInitializer extends AbstractDynamicConfigurationInitializer {
    public JdkProxyDynamicConfigurationInitializer(
            Object configuration,
            ConfigurationModel configurationModel,
            ClassLoader classLoader,
            ConfigurationInstanceCreator configurationInstanceCreator, TypeConverter<?> typeConverter,
            ConfigurationValuesSource valuesSource,
            KeyGenerator keyGenerator,
            String fallbackKeyPrefix,
            Map<String, String> defaultValues,
            Attributes customAttributes,
            ConfigurationValueProvider configurationValueProvider) {
        super(configuration, configurationModel, classLoader, configurationInstanceCreator, typeConverter, valuesSource,
                keyGenerator, fallbackKeyPrefix, defaultValues, customAttributes, configurationValueProvider);
    }

    @Override
    protected ConfigurationInitializer createSubConfigurationInitializer(
            Object subConfiguration, ConfigurationModel configurationModel,
            KeyGenerator keyGenerator, String fallbackKey, Map<String, String> defaultValues, Attributes customAttributes) {

        return new JdkProxyDynamicConfigurationInitializer(
                subConfiguration, configurationModel, classLoader, configurationInstanceCreator, typeConverter, valuesSource,
                keyGenerator, fallbackKey, defaultValues, Attributes.merge(configurationModel.getCustomAttributes(), customAttributes),
                configurationValueProvider);
    }

    @Override
    protected ConfigurationPropertiesAccessor getConfigurationPropertiesAccessor() {
        return getInvocationHandler();
    }

    @Override
    protected void processConfiguration(ConfigurationModel configurationModel) {
        super.processConfiguration(configurationModel);

        JdkProxyDynamicConfigurationInvocationHandler invocationHandler = getInvocationHandler();
        invocationHandler.setConfigurationValuesSource(valuesSource);
        invocationHandler.setTypeConverter(typeConverter);
        invocationHandler.setConfigurationValueProvider(configurationValueProvider);
    }

    private JdkProxyDynamicConfigurationInvocationHandler getInvocationHandler() {
        return (JdkProxyDynamicConfigurationInvocationHandler) Proxy.getInvocationHandler(configuration);
    }
}
