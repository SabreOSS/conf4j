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

package com.sabre.oss.conf4j.spring.factory.cglib;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.internal.config.DefaultConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;
import com.sabre.oss.conf4j.spring.internal.factory.cglib.CglibStaticConfigurationInitializer;
import com.sabre.oss.conf4j.spring.internal.factory.cglib.CglibStaticConfigurationInstanceCreator;

import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.emptyKeyGenerator;
import static java.util.Collections.emptyMap;


/**
 * Configuration factory which creates configuration instances which are <i>static</i>.
 * Configuration values associated with a given configuration property are stored in the corresponding fields
 * (one for each configuration property).
 * Once the configuration has been created, values within these fields do not change.
 * <p>
 * Once the configuration instance is created, {@link ConfigurationValuesSource} associated with the factory
 * is never accessed again.
 * </p>
 * <p>
 * The generated configuration class implements {@link java.io.Serializable} which allows configuration instance
 * to be serialized using standard java serialization. It requires that all of the configuration property values
 * provided by {@link TypeConverter} be serializable. The framework does not currently validate these values;
 * though, this may change in the future.
 * </p>
 * <p>
 * <b>Note:</b> This configuration factory is using CGLIB proxies.
 * </p>
 *
 * @see ConfigurationFactory
 */
public class CglibStaticConfigurationFactory extends AbstractConfigurationFactory {
    public CglibStaticConfigurationFactory() {
        super(new CglibStaticConfigurationInstanceCreator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeConfiguration(
            Object configurationInstance, ConfigurationModel configurationModel,
            ConfigurationValuesSource valuesSource, ClassLoader classLoader) {

        new CglibStaticConfigurationInitializer(
                configurationInstance, configurationModel,
                classLoader, configurationInstanceCreator,
                typeConverter,
                valuesSource,
                emptyKeyGenerator(),
                null,
                emptyMap(),
                configurationModel.getCustomAttributes(),
                new DefaultConfigurationValueProvider(configurationValueProcessors)
        ).initializeConfiguration();
    }
}

