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

import com.sabre.oss.conf4j.source.ConfigurationEntry;
import com.sabre.oss.conf4j.source.IterableConfigurationSource;
import com.sabre.oss.conf4j.source.MapIterable;
import com.sabre.oss.conf4j.source.OptionalValue;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
    /**
     * Property name used when YAML does not represent map.
     * <p>
     * For example:
     * <pre class="code">
     * some string
     * </pre>
     * is transformed into:
     * <pre class="code">
     * document=some string
     * </pre>
     */
    public static final String DEFAULT_PROPERTY = "document";

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

        try (Reader reader = new UnicodeReader(inputStream)) {
            this.properties = createProperties(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to process YAML.", e);
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
            this.properties = createProperties(reader);
            reader.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to process YAML.", e);
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

        try (Reader reader = new FileReader(file)) {
            this.properties = createProperties(reader);
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

    private Map<String, String> createProperties(Reader reader) {
        Yaml yaml = new Yaml();
        Object yamlContent = yaml.load(reader);
        Map<String, String> result = new LinkedHashMap<>();
        buildFlattenedMap(result, createMap(yamlContent), null);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createMap(Object object) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            result.put(DEFAULT_PROPERTY, object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                value = createMap(value);
            }

            String mapKey = key instanceof Number
                    ? keyIndex(((Number) key).intValue())
                    : key.toString();

            result.put(mapKey, value);
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private void buildFlattenedMap(Map<String, String> result, Map<String, Object> source, String path) {
        source.forEach((key, value) -> {
            if (isNotEmpty(path)) {
                key = (key.charAt(0) == '[') ? (path + key) : (path + '.' + key);
            }
            if (value instanceof String) {
                result.put(key, (String) value);
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                int idx = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result, singletonMap(keyIndex(idx++), object), key);
                }
            } else {
                result.put(key, Objects.toString(value, EMPTY));
            }
        });
    }

    private String keyIndex(int idx) {
        return "[" + idx + ']';
    }
}
