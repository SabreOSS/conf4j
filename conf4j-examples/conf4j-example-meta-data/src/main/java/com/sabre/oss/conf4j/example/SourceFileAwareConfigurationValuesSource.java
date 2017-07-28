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

package com.sabre.oss.conf4j.example;

import com.sabre.oss.conf4j.source.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SourceFileAwareConfigurationValuesSource implements ConfigurationValuesSource {
    public static final String FILE = "file";
    public static final String DEFAULT_PROPERTIES = "application.properties";

    private final Map<String, ConfigurationValuesSource> sources = new ConcurrentHashMap<>();

    @Override
    public OptionalValue<String> getValue(String key) {
        return getValue(key, null);
    }

    @Override
    public OptionalValue<String> getValue(String key, Attributes attributes) {
        return getConfigurationValuesSource(attributes).getValue(key, attributes);
    }

    @Override
    public ConfigurationEntry findEntry(Collection<String> keys) {
        return findEntry(keys, null);
    }

    @Override
    public ConfigurationEntry findEntry(Collection<String> keys, Attributes attributes) {
        return getConfigurationValuesSource(attributes).findEntry(keys, attributes);
    }

    private ConfigurationValuesSource getConfigurationValuesSource(Attributes attributes) {
        String fileAttributeValue = (attributes == null) ? null : attributes.getAttributes().get(FILE);
        if (fileAttributeValue == null) {
            fileAttributeValue = DEFAULT_PROPERTIES;
        }
        String file = getClass().getResource('/' + fileAttributeValue).getFile();
        return sources.computeIfAbsent(file, PropertiesConfigurationValuesSource::new);
    }
}
