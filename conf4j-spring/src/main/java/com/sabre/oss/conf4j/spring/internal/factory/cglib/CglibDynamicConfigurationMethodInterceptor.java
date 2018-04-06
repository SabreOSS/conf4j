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

package com.sabre.oss.conf4j.spring.internal.factory.cglib;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.config.ConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.config.DefaultDynamicConfiguration;
import com.sabre.oss.conf4j.internal.config.DynamicConfiguration;
import com.sabre.oss.conf4j.internal.config.PropertyMetadata;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import com.sabre.oss.conf4j.source.OptionalValue;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import static com.sabre.oss.conf4j.internal.Constants.COLLECTION_SIZE_SUFFIX;
import static com.sabre.oss.conf4j.internal.utils.PropertyUtils.getPropertyName;

class CglibDynamicConfigurationMethodInterceptor extends AbstractCglibConfigurationMethodInterceptor {
    private final DynamicConfiguration dynamicConfiguration = new DefaultDynamicConfiguration();

    CglibDynamicConfigurationMethodInterceptor(ConfigurationModel configurationModel) {
        super(configurationModel);
    }

    void setTypeConverter(TypeConverter<?> typeConverter) {
        this.dynamicConfiguration.setTypeConverter(typeConverter);
    }

    void setConfigurationSource(ConfigurationSource configurationSource) {
        this.dynamicConfiguration.setConfigurationSource(configurationSource);
    }

    void setConfigurationValueProvider(ConfigurationValueProvider configurationValueProvider) {
        this.dynamicConfiguration.setConfigurationValueProvider(configurationValueProvider);
    }

    void setParentConfiguration(DynamicConfiguration parentConfiguration) {
        this.dynamicConfiguration.setParentConfiguration(parentConfiguration);
    }

    @Override
    protected Object interceptInternal(Object obj, Method method, Object[] args, MethodProxy proxy) {
        if (method.getDeclaringClass() == DynamicConfiguration.class) {
            return invokeDynamicConfigurationMethod(method, args);
        }

        String propertyName = getPropertyName(method);
        if (subConfigurationProperties.contains(propertyName)) {
            // returns sub-configuration
            return getSubConfigurationProperty(propertyName);
        }

        TypeConverter<?> typeConverter = dynamicConfiguration.getTypeConverter();
        ConfigurationSource configurationSource = dynamicConfiguration.getConfigurationSource();
        ConfigurationValueProvider configurationValueProvider = dynamicConfiguration.getConfigurationValueProvider();
        if (subConfigurationListProperties.contains(propertyName)) {
            // return sub-configuration list
            String sizePropertyName = propertyName + COLLECTION_SIZE_SUFFIX;
            PropertyMetadata listSizePropertyMetadata = getPropertyMetadata(sizePropertyName);
            OptionalValue<?> size = configurationValueProvider.getConfigurationValue(typeConverter, configurationSource, listSizePropertyMetadata);
            int actualSize = (Integer) (size.isPresent() ? size.get() : getValueProperty(sizePropertyName));
            return getSubConfigurationListProperty(propertyName).asUnmodifiableList(actualSize);
        }

        // return value property
        PropertyMetadata metadata = getPropertyMetadata(propertyName);
        OptionalValue<?> configurationValue = configurationValueProvider.getConfigurationValue(typeConverter, configurationSource, metadata);
        return configurationValue.isPresent() ? configurationValue.get() : getValueProperty(propertyName);
    }

    private Object invokeDynamicConfigurationMethod(Method method, Object[] args) {
        switch (method.getName()) {
            case "getTypeConverter":
                return this.dynamicConfiguration.getTypeConverter();
            case "setTypeConverter":
                this.dynamicConfiguration.setTypeConverter((TypeConverter<?>) args[0]);
                return null;
            case "getConfigurationSource":
                return this.dynamicConfiguration.getConfigurationSource();
            case "setConfigurationSource":
                this.dynamicConfiguration.setConfigurationSource((ConfigurationSource) args[0]);
                return null;
            case "getConfigurationValueProvider":
                return this.dynamicConfiguration.getConfigurationValueProvider();
            case "setConfigurationValueProvider":
                this.dynamicConfiguration.setConfigurationValueProvider((ConfigurationValueProvider) args[0]);
                return null;
            case "getParentConfiguration":
                return this.dynamicConfiguration.getParentConfiguration();
            case "setParentConfiguration":
                this.dynamicConfiguration.setParentConfiguration((DynamicConfiguration) args[0]);
                return null;
            default:
                throw new IllegalStateException("Unknown method: " + method);
        }
    }
}
