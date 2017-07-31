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

package com.sabre.oss.conf4j.factory.javassist;

import com.sabre.oss.conf4j.converter.DefaultTypeConverters;
import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.internal.config.DefaultConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.internal.factory.javassist.JavassistDynamicConfigurationInitializer;
import com.sabre.oss.conf4j.internal.factory.javassist.JavassistDynamicConfigurationInstanceCreator;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;

import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.emptyKeyGenerator;
import static java.util.Collections.emptyMap;

/**
 * Configuration factory which creates configuration instances which are <i>dynamic</i>. Each time
 * the configuration property is accessed framework delegates to {@link ConfigurationValuesSource} to find the appropriate
 * configuration value associated with one of the configuration keys and then the value is converted to the
 * configuration property type by the {@link TypeConverter}.
 *
 * @see ConfigurationFactory
 */
public class JavassistDynamicConfigurationFactory extends AbstractConfigurationFactory {
    /**
     * Constructs configuration factory using default {@link TypeConverter}
     * as provided by {@link DefaultTypeConverters#getDefaultTypeConverter()}.
     */
    public JavassistDynamicConfigurationFactory() {
        super(new JavassistDynamicConfigurationInstanceCreator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeConfiguration(Object configurationInstance, ConfigurationModel configurationModel, ConfigurationValuesSource valuesSource, ClassLoader classLoader) {
        new JavassistDynamicConfigurationInitializer(
                configurationInstance, configurationModel,
                classLoader, configurationInstanceCreator,
                typeConverter,
                valuesSource,
                emptyKeyGenerator(),
                null,
                emptyMap(),
                configurationModel.getAttributes(),
                new DefaultConfigurationValueProvider(configurationValueProcessors)
        ).initializeConfiguration();
    }
}
