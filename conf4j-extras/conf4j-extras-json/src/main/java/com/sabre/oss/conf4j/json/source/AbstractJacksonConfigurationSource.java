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

package com.sabre.oss.conf4j.json.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.sabre.oss.conf4j.source.ConfigurationEntry;
import com.sabre.oss.conf4j.source.IterableConfigurationSource;
import com.sabre.oss.conf4j.source.MapIterable;
import com.sabre.oss.conf4j.source.OptionalValue;

import java.io.*;
import java.util.Map;

import static com.sabre.oss.conf4j.json.source.FlattenerUtils.toFlatMap;
import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Configuration source which which uses Jackson for converting source file to map.
 * Subclasses can provide support for formats supported by Jackson, for example JSON or YAML.
 */
public abstract class AbstractJacksonConfigurationSource implements IterableConfigurationSource {
    private final Map<String, String> properties;

    /**
     * Constructs value source from {@link InputStream}.
     * <p>
     * <em>Note:</em> File content is loaded and processed in the constructor and {@code inputStream} is closed
     * at the end of processing.
     *
     * @param objectMapper configured object mapper.
     * @param inputStream  input stream which provides JSON.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    protected AbstractJacksonConfigurationSource(ObjectMapper objectMapper, InputStream inputStream) {
        requireNonNull(inputStream, "inputStream cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object content = objectReader.readValue(inputStream);
            this.properties = toFlatMap(content);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to process file.", e);
        }
    }

    /**
     * Constructs value source from {@link Reader}.
     * <p>
     * <em>Note:</em> File content is loaded and processed in the constructor and {@code reader} is closed
     * at the end of processing.
     *
     * @param objectMapper configured object mapper.
     * @param reader       reader which provides JSON source.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    protected AbstractJacksonConfigurationSource(ObjectMapper objectMapper, Reader reader) {
        requireNonNull(reader, "reader cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object content = objectReader.readValue(reader);
            this.properties = toFlatMap(content);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to process file.", e);
        }
    }

    /**
     * Constructs value source from {@link File}.
     *
     * @param objectMapper configured object mapper.
     * @param file         file which contains JSON source.
     * @throws IllegalArgumentException when provided file not found.
     * @throws UncheckedIOException     when {@link IOException} is thrown during processing.
     */
    protected AbstractJacksonConfigurationSource(ObjectMapper objectMapper, File file) {
        requireNonNull(file, "file cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object content = objectReader.readValue(file);
            this.properties = toFlatMap(content);
        } catch (IOException e) {
            throw new UncheckedIOException(format("Unable to process '%s'.", file), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionalValue<String> getValue(String key, Map<String, String> attributes) {
        return properties.containsKey(key)
                ? present(properties.get(key))
                : absent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ConfigurationEntry> getAllConfigurationEntries() {
        return new MapIterable(properties);
    }
}
