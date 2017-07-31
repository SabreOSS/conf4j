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

package com.sabre.oss.conf4j.internal.factory;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.config.ConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.config.PropertyMetadata;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.utils.KeyGenerator;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;

import java.util.Map;

public abstract class AbstractStaticConfigurationInitializer extends AbstractConfigurationInitializer {
    protected AbstractStaticConfigurationInitializer(
            Object configuration,
            ConfigurationModel configurationModel,
            ClassLoader classLoader,
            ConfigurationInstanceCreator configurationInstanceCreator, TypeConverter<?> typeConverter,
            ConfigurationValuesSource valuesSource,
            KeyGenerator keyGenerator,
            String fallbackKeyPrefix,
            Map<String, String> defaultValues,
            Map<String, String> attributes,
            ConfigurationValueProvider configurationValueProvider) {
        super(configuration, configurationModel, classLoader, configurationInstanceCreator, typeConverter, valuesSource,
                keyGenerator, fallbackKeyPrefix, defaultValues, attributes, configurationValueProvider);
    }

    @Override
    protected void storePropertyMetadata(PropertyMetadata propertyMetadata) {
        // Don't store metadata - it is not required by static configuration. Store only value resolved from source.
        Object value = configurationValueProvider.getConfigurationValue(typeConverter, valuesSource, propertyMetadata).getOrNull();
        getConfigurationPropertiesAccessor().setValueProperty(propertyMetadata.getPropertyName(), value);
    }
}
