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

package com.sabre.oss.conf4j.yaml.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sabre.oss.conf4j.source.ConfigurationEntry;
import com.sabre.oss.conf4j.source.IterableConfigurationSource;
import com.sabre.oss.conf4j.source.MapIterable;
import com.sabre.oss.conf4j.source.OptionalValue;

import java.io.*;
import java.util.Map;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;
import static com.sabre.oss.conf4j.json.source.NormalizationUtils.normalizeToMap;
import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Configuration source which supports YAML. It flattens the YAML structure to key-value properties.
 * <p>
 * Hierarchical objects are exposed by nested paths.
 * <p>
 * For example:
 * <pre class="code">
 * &nbsp; users:
 * &nbsp;   admin:
 * &nbsp;     name: John Smith
 * &nbsp;     age: 30
 * &nbsp;     country: USA
 * </pre>
 * is transformed into these properties:
 * <pre class="code">
 * users.admin.name=John Smith
 * users.admin.age=30
 * users.admin.country=USA
 * </pre>
 * {@code []} brackets are used to split lists into property keys.
 * <p>
 * For example:
 * <pre class="code">
 * continents:
 * &nbsp;  - Asia
 * &nbsp;  - Africa
 * &nbsp;  - North America
 * &nbsp;  - South America
 * &nbsp;  - Antarctica
 * &nbsp;  - Europe
 * &nbsp;  - Australia
 * </pre>
 * is transformed into these properties:
 * <pre class="code">
 * continents[0]=Asia
 * continents[1]=Africa
 * continents[2]=North America
 * continents[3]=South America
 * continents[4]=Antarctica
 * continents[5]=Europe
 * continents[6]=Australia
 * </pre>
 */
public class YamlConfigurationSource implements IterableConfigurationSource {
    private final ObjectMapper objectMapper = new ObjectMapper(
            new YAMLFactory().enable(MINIMIZE_QUOTES).disable(WRITE_DOC_START_MARKER));
    private final Map<String, String> properties;

    /**
     * Constructs value source from {@link InputStream}.
     * <p>
     * <em>Note:</em> YAML is loaded and processed in the constructor and {@code inputStream} is closed
     * at the end of processing.
     *
     * @param inputStream input stream which provides YAML.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    public YamlConfigurationSource(InputStream inputStream) {
        requireNonNull(inputStream, "inputStream cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object yamlContent = objectReader.readValue(inputStream);
            this.properties = normalizeToMap(yamlContent);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to process YAML.", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to close inputStream.", e);
            }
        }
    }

    /**
     * Constructs value source from {@link Reader}.
     * <p>
     * <em>Note:</em> YAML is loaded and processed in the constructor and {@code inputStream} is closed
     * at the end of processing.
     *
     * @param reader reader which provides YAML source.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    public YamlConfigurationSource(Reader reader) {
        requireNonNull(reader, "reader cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object yamlContent = objectReader.readValue(reader);
            this.properties = normalizeToMap(yamlContent);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to process YAML.", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to close reader.", e);
            }
        }
    }

    /**
     * Constructs value source from {@link File}.
     *
     * @param file file which contains YAML source.
     * @throws IllegalArgumentException when provided file not found.
     * @throws UncheckedIOException     when {@link IOException} is thrown during processing.
     */
    public YamlConfigurationSource(File file) {
        requireNonNull(file, "file cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object yamlContent = objectReader.readValue(file);
            this.properties = normalizeToMap(yamlContent);
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
