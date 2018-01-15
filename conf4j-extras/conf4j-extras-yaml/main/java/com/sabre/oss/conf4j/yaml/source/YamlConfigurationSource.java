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

package com.sabre.oss.conf4j.yaml.source;

import com.sabre.oss.conf4j.source.ConfigurationEntry;
import com.sabre.oss.conf4j.source.IterableConfigurationSource;
import com.sabre.oss.conf4j.source.MapIterable;
import com.sabre.oss.conf4j.source.OptionalValue;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

/**
 * Configuration source created by reading YAML source. It flattens YAML structure to String property values.
 * <p>
 * Hierarchical objects are exposed by nested paths.
 * <b>For example:</b>
 * <p>
 * <pre class="code">
 * users:
 * admin:
 * name: John Smith
 * age: 30
 * country: USA
 * </pre>
 * <p>
 * is transformed into these properties:
 * <p>
 * <pre class="code">
 * users.admin.name=John Smith
 * users.admin.age=30
 * users.admin.country=USA
 * </pre>
 * <p>
 * <code>[]</code> dereferencers are used to split lists into property keys.
 * <b>For example:</b>
 * <pre class="code">
 * continents:
 * - Asia
 * - Africa
 * - North America
 * - South America
 * - Antarctica
 * - Europe
 * - Australia
 * </pre>
 * <p>
 * is transformed into these properties:
 * <p>
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
    private final Map<String, String> properties;

    /**
     * Create YamlConfigurationSource instance.
     *
     * @param inputStream input stream of YAML source.
     * @throws UncheckedIOException when unable to properly close {@param inputStream}.
     */
    public YamlConfigurationSource(InputStream inputStream) {
        requireNonNull(inputStream, "inputStream cannot be null");

        try (Reader reader = new UnicodeReader(inputStream)) {
            this.properties = createProperties(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to release resource.", e);
        }
    }

    /**
     * Create YamlConfigurationSource instance.
     *
     * @param reader reader for YAML source.
     * @throws UncheckedIOException when unable to properly close {@param reader}.
     */
    public YamlConfigurationSource(Reader reader) {
        requireNonNull(reader, "reader cannot be null");

        try {
            this.properties = createProperties(reader);
            reader.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to release resource.", e);
        }
    }

    /**
     * Create YamlConfigurationSource instance.
     *
     * @param file file representing YAML source.
     * @throws IllegalArgumentException when provided file not found.
     * @throws UncheckedIOException     when unable to properly close {@link FileReader} for {@param file}.
     */
    public YamlConfigurationSource(File file) {
        requireNonNull(file, "file cannot be null");

        try (Reader reader = new FileReader(file)) {
            this.properties = createProperties(reader);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Provided file does not exist.", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to release resource.", e);
        }
    }

    @Override
    public OptionalValue<String> getValue(String key, Map<String, String> attributes) {
        return properties.containsKey(key)
                ? present(properties.get(key))
                : absent();
    }

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
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = createMap(value);
            }
            Object key = entry.getKey();
            if (key instanceof String) {
                result.put(key.toString(), value);
            } else {
                result.put('[' + key.toString() + ']', value);
            }
        }
        return result;
    }

    private void buildFlattenedMap(Map<String, String> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isEmpty(path)) {
                if (key.charAt(0) == '[') {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, (String) value);
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result, singletonMap("[" + (count++) + ']', object), key);
                }
            } else {
                result.put(key, (value != null ? value.toString() : ""));
            }
        }
    }
}
