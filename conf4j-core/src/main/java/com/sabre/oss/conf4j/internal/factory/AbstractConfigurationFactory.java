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

package com.sabre.oss.conf4j.internal.factory;

import com.sabre.oss.conf4j.converter.DefaultTypeConverters;
import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.model.ConfigurationModelProvider;
import com.sabre.oss.conf4j.internal.model.provider.convention.ConventionConfigurationModelProvider;
import com.sabre.oss.conf4j.processor.ConfigurationValueProcessor;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.apache.commons.lang3.Validate.noNullElements;

/**
 * Skeleton implementation of the {@link ConfigurationFactory} which provides common logic for implementing
 * configuration factory. It delegates to the configuration class parser and configuration implementer.
 */
public abstract class AbstractConfigurationFactory implements ConfigurationFactory {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Configuration class generator.
     */
    protected final ConfigurationInstanceCreator configurationInstanceCreator;

    /**
     * Configuration model provider.
     */
    protected ConfigurationModelProvider configurationModelProvider = ConventionConfigurationModelProvider.getInstance();

    /**
     * List of {@link ConfigurationValueProcessor} which allows augmenting configuration value get from
     * the configuration value source for example for decrypting or uncompressing it.
     */
    protected List<ConfigurationValueProcessor> configurationValueProcessors = emptyList();

    /**
     * {@link TypeConverter} used for converting string configuration values to appropriate type.
     */
    protected TypeConverter<?> typeConverter = DefaultTypeConverters.getDefaultTypeConverter();

    /**
     * Constructs configuration factory using default {@link TypeConverter}
     * as provided by {@link DefaultTypeConverters#getDefaultTypeConverter()}. The converter can be customized later by
     * {@link #setTypeConverter(TypeConverter)}.
     * By default none {@link ConfigurationValueProcessor} is registered, use {@link #setConfigurationValueProcessors(List)}
     * to set configuration value processors after factory instantiation.
     *
     * @param configurationInstanceCreator configuration class generator used for generating implementation of configuration classes.
     */
    protected AbstractConfigurationFactory(ConfigurationInstanceCreator configurationInstanceCreator) {
        this.configurationInstanceCreator = requireNonNull(configurationInstanceCreator, "configurationInstanceCreator cannot be null");
    }

    /**
     * Sets configuration model provider used for building configuration model from the configuration type.
     *
     * @param configurationModelProvider configuration model provider.
     * @throws NullPointerException when {@code configurationModelProvider} is {@code null}.
     */
    public void setConfigurationModelProvider(ConfigurationModelProvider configurationModelProvider) {
        this.configurationModelProvider = requireNonNull(configurationModelProvider, "configurationModelProvider cannot be null");
    }

    /**
     * Sets type converter which is used for converting string configuration value to appropriate type.
     *
     * @param typeConverter type converter
     * @throws NullPointerException when {@code typeConverter} is {@code null}.
     */
    public void setTypeConverter(TypeConverter<?> typeConverter) {
        this.typeConverter = requireNonNull(typeConverter, "typeConverter cannot be null");
    }

    /**
     * Sets list of {@link ConfigurationValueProcessor}.
     *
     * @param configurationValueProcessors list of configuration value processors
     * @throws NullPointerException when {@code configurationValueProcessors} is {@code null} or any of processors is {@code null}.
     */
    public void setConfigurationValueProcessors(List<ConfigurationValueProcessor> configurationValueProcessors) {
        requireNonNull(configurationValueProcessors, "configurationValueProcessors cannot be null");
        noNullElements(configurationValueProcessors, "configurationValueProcessors element cannot be null");
        this.configurationValueProcessors = configurationValueProcessors;
    }

    /**
     * Initializes {@code configurationInstance}
     *
     * @param configurationInstance configuration instance for initialization.
     * @param configurationModel    configuration model associated with the configuration instance.
     * @param configurationSource   values source used for initialization.
     * @param classLoader           class loaded which is used for loading classes when necessary.
     */
    protected abstract void initializeConfiguration(Object configurationInstance, ConfigurationModel configurationModel, ConfigurationSource configurationSource, ClassLoader classLoader);

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createConfiguration(Class<T> configurationType, ConfigurationSource configurationSource) {
        return createConfiguration(configurationType, configurationSource, Thread.currentThread().getContextClassLoader());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createConfiguration(Class<T> configurationType, ConfigurationSource configurationSource, ClassLoader classLoader) {
        requireNonNull(configurationType, "configurationType cannot be null");
        requireNonNull(configurationSource, "configurationSource cannot be null");

        long start = nanoTime();

        ConfigurationModel configurationModel = configurationModelProvider.getConfigurationModel(configurationType);

        if (configurationModel.isAbstractConfiguration()) {
            throw new IllegalArgumentException(format("Configuration type %s is marked as abstract configuration and creating an configuration instance is forbidden.",
                    configurationModel.getConfigurationType().getName()));
        }

        ClassLoader actualClassLoader = (classLoader != null) ? classLoader : Thread.currentThread().getContextClassLoader();
        Object configurationInstance = configurationInstanceCreator.createInstance(configurationModel, actualClassLoader);
        initializeConfiguration(configurationInstance, configurationModel, configurationSource, actualClassLoader);

        log.trace("Configuration {} created in {} us", configurationType.getSimpleName(), NANOSECONDS.toMicros(nanoTime() - start));

        return configurationType.cast(configurationInstance);
    }
}
