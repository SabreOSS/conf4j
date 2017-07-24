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

package com.sabre.oss.conf4j.internal.config;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;

/**
 * {@link DynamicConfiguration} implementation which holds {@link TypeConverter}, {@link ConfigurationValuesSource}
 * {@link ConfigurationValueProvider} and parent {@link DynamicConfiguration} as internal fields.
 */
public class DefaultDynamicConfiguration implements DynamicConfiguration {
    private TypeConverter<?> typeConverter;
    private ConfigurationValuesSource configurationValuesSource;
    private ConfigurationValueProvider configurationValueProvider;
    private DynamicConfiguration parentConfiguration;

    @Override
    public TypeConverter<?> getTypeConverter() {
        if (typeConverter == null && parentConfiguration != null) {
            typeConverter = parentConfiguration.getTypeConverter();
        }
        return typeConverter;
    }

    @Override
    public void setTypeConverter(TypeConverter<?> typeConverter) {
        this.typeConverter = typeConverter;
    }

    @Override
    public ConfigurationValuesSource getConfigurationValuesSource() {
        if (configurationValuesSource == null && parentConfiguration != null) {
            configurationValuesSource = parentConfiguration.getConfigurationValuesSource();
        }
        return configurationValuesSource;
    }

    @Override
    public void setConfigurationValuesSource(ConfigurationValuesSource configurationValuesSource) {
        this.configurationValuesSource = configurationValuesSource;
    }

    @Override
    public ConfigurationValueProvider getConfigurationValueProvider() {
        if (configurationValueProvider == null && parentConfiguration != null) {
            configurationValueProvider = parentConfiguration.getConfigurationValueProvider();
        }
        return configurationValueProvider;
    }

    @Override
    public void setConfigurationValueProvider(ConfigurationValueProvider configurationValueProvider) {
        this.configurationValueProvider = configurationValueProvider;
    }

    @Override
    public DynamicConfiguration getParentConfiguration() {
        return parentConfiguration;
    }

    @Override
    public void setParentConfiguration(DynamicConfiguration parentConfiguration) {
        this.parentConfiguration = parentConfiguration;
    }
}
