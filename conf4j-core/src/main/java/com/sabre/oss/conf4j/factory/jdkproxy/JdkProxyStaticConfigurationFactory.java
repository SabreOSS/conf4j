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

package com.sabre.oss.conf4j.factory.jdkproxy;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.internal.config.DefaultConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.factory.jdkproxy.AbstractJdkProxyConfigurationFactory;
import com.sabre.oss.conf4j.internal.factory.jdkproxy.JdkProxyStaticConfigurationInitializer;
import com.sabre.oss.conf4j.internal.factory.jdkproxy.JdkProxyStaticConfigurationInstanceCreator;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.source.ConfigurationSource;

import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.emptyKeyGenerator;
import static java.util.Collections.emptyMap;

/**
 * Configuration factory which creates configuration instances which are <i>static</i>.
 * Configuration values associated with a given configuration property are stored in the corresponding fields
 * (one for each configuration property).
 * Once the configuration has been created, values within these fields do not change.
 * <p>
 * Once the configuration instance is created, {@link ConfigurationSource} associated with the factory
 * is never accessed again.
 * <p>
 * The generated configuration class implements {@link java.io.Serializable} which allows configuration instance
 * to be serialized using standard java serialization. It requires that all of the configuration property values
 * provided by {@link TypeConverter} be serializable. The framework does not currently validate these values;
 * though, this may change in the future.
 * <p>
 * <b>Note:</b> This configuration factory is using JDK proxies. Because JDK proxy supports only interfaces,
 * configuration types based on abstract classes are not supported.
 *
 * @see ConfigurationFactory
 */
public class JdkProxyStaticConfigurationFactory extends AbstractJdkProxyConfigurationFactory {
    public JdkProxyStaticConfigurationFactory() {
        super(new JdkProxyStaticConfigurationInstanceCreator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeConfiguration(
            Object configurationInstance, ConfigurationModel configurationModel,
            ConfigurationSource configurationSource, ClassLoader classLoader) {

        new JdkProxyStaticConfigurationInitializer(
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
