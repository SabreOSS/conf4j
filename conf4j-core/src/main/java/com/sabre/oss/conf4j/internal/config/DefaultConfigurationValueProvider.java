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

package com.sabre.oss.conf4j.internal.config;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.processor.ConfigurationValue;
import com.sabre.oss.conf4j.processor.ConfigurationValueProcessor;
import com.sabre.oss.conf4j.source.ConfigurationEntry;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;
import com.sabre.oss.conf4j.source.OptionalValue;

import java.util.List;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class DefaultConfigurationValueProvider implements ConfigurationValueProvider {
    private final List<ConfigurationValueProcessor> configurationValueProcessors;

    public DefaultConfigurationValueProvider(List<ConfigurationValueProcessor> configurationValueProcessors) {
        this.configurationValueProcessors = requireNonNull(configurationValueProcessors, "configurationValueProcessors cannot be null");
    }

    @Override
    public <T> OptionalValue<T> getConfigurationValue(TypeConverter<T> typeConverter, ConfigurationValuesSource valuesSource, PropertyMetadata metadata) {
        requireNonNull(typeConverter, "typeConverter cannot be null");
        requireNonNull(metadata, "metadata cannot be null");

        OptionalValue<String> value = absent();
        String resolvedKey = null;

        if (valuesSource != null) {
            ConfigurationEntry configurationEntry = valuesSource.findEntry(metadata.getKeySet(), metadata.getCustomAttributes());
            if (configurationEntry != null) {
                resolvedKey = configurationEntry.getKey();
                value = present(configurationEntry.getValue());
            }
        }

        boolean fromDefaultValue = value.isAbsent();
        if (fromDefaultValue) {
            value = metadata.getDefaultValue();
        }
        if (value.isAbsent()) {
            // value is not available in the value source nor from the default
            return absent();
        }
        String resolvedValue = value.get();
        String val = applyProcessors(new ConfigurationValue(resolvedKey, resolvedValue, fromDefaultValue, metadata.getEncryptionProvider(), metadata.getCustomAttributes()));

        TypeConverter<T> currentTypeConverter = defaultIfNull((TypeConverter<T>) metadata.getTypeConverter(), typeConverter);
        return present(currentTypeConverter.fromString(metadata.getType(), val));
    }

    private String applyProcessors(ConfigurationValue configurationValue) {
        if (configurationValueProcessors == null) {
            return configurationValue.getValue();
        }

        ConfigurationValue currentConfigurationValue = configurationValue;
        for (ConfigurationValueProcessor configurationValueProcessor : configurationValueProcessors) {
            currentConfigurationValue = configurationValueProcessor.process(currentConfigurationValue);
        }
        if (currentConfigurationValue.isEncrypted()) {
            throw new IllegalStateException("Configuration value cannot be decrypted. Please check if the appropriate decrypter is configured.");
        }

        return currentConfigurationValue.getValue();
    }
}
