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
package com.sabre.oss.conf4j.source;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Objects.requireNonNull;

/**
 * Configuration value source backed by {@code Properties}.
 * <p>
 * It is <i>thread safe</i>.
 * </p>
 */
public class PropertiesConfigurationValuesSource implements IterableConfigurationValuesSource {
    protected final Properties source;

    /**
     * Constructs values source from {@link Properties}.
     *
     * @param source properties which holds configuration keys.
     * @throws NullPointerException when {@code source} is null.
     */
    public PropertiesConfigurationValuesSource(Properties source) {
        this.source = requireNonNull(source, "source cannot be null");
    }


    /**
     * Constructs values source from a property file.
     *
     * @param propertyFile name of property file.
     * @throws IllegalArgumentException when the format of a property file is invalid.
     * @throws UncheckedIOException     when {@link IOException} thrown while loading the property file.
     */
    public PropertiesConfigurationValuesSource(String propertyFile) {
        requireNonNull(propertyFile, "propertyFile cannot be null");
        this.source = loadFromFile(propertyFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionalValue<String> getValue(String key) {
        requireNonNull(key, "key cannot be null");

        String value = source.getProperty(key);

        return value != null || source.containsKey(key) ? present(value) : absent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ConfigurationEntry> getAllConfigurationEntries() {
        return new PropertiesIterable(source);
    }

    protected static Properties loadFromFile(String propertyFile) {
        try (FileReader reader = new FileReader(propertyFile)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class PropertiesIterable implements Iterable<ConfigurationEntry> {
        private final Properties properties;

        private PropertiesIterable(Properties properties) {
            this.properties = properties;
        }

        @Override
        public Iterator<ConfigurationEntry> iterator() {
            Iterator<Entry<Object, Object>> iterator = properties.entrySet().iterator();
            return new Iterator<ConfigurationEntry>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public ConfigurationEntry next() {
                    Entry<Object, Object> next = iterator.next();
                    Object key = next.getKey();
                    Object value = next.getValue();
                    return new ConfigurationEntry(key.toString(), value instanceof String ? (String) value : null);
                }
            };
        }
    }

}
