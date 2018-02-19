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

package com.sabre.oss.conf4j.spring.factory.cglib;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.internal.config.DefaultConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import com.sabre.oss.conf4j.spring.internal.factory.cglib.CglibDynamicConfigurationInitializer;
import com.sabre.oss.conf4j.spring.internal.factory.cglib.CglibDynamicConfigurationInstanceCreator;

import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.emptyKeyGenerator;
import static java.util.Collections.emptyMap;

/**
 * Configuration factory which creates configuration instances which are <i>dynamic</i>. Each time
 * the configuration property is accessed, framework delegates to {@link ConfigurationSource}
 * to find an appropriate configuration value associated with one of the configuration keys
 * and then the value is converted to the configuration property type by the {@link TypeConverter}.
 * <p>
 * <b>Note:</b> This configuration factory is using CGLIB proxies.
 *
 * @see ConfigurationFactory
 */
public class CglibDynamicConfigurationFactory extends AbstractConfigurationFactory {
    public CglibDynamicConfigurationFactory() {
        super(new CglibDynamicConfigurationInstanceCreator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeConfiguration(
            Object configurationInstance, ConfigurationModel configurationModel,
            ConfigurationSource configurationSource, ClassLoader classLoader) {

        new CglibDynamicConfigurationInitializer(
                configurationInstance, configurationModel,
                classLoader, configurationInstanceCreator,
                typeConverter,
                configurationSource,
                emptyKeyGenerator(),
                null,
                emptyMap(),
                configurationModel.getAttributes(),
                new DefaultConfigurationValueProvider(configurationValueProcessors)
        ).initializeConfiguration();
    }
}

