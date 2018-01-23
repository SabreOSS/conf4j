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

package com.sabre.oss.conf4j.json.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.sabre.oss.conf4j.source.ConfigurationEntry;
import com.sabre.oss.conf4j.source.IterableConfigurationSource;
import com.sabre.oss.conf4j.source.MapIterable;
import com.sabre.oss.conf4j.source.OptionalValue;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

/**
 * Configuration source which supports JSON. It flattens the JSON structure to key-value properties.
 * <p>
 * Hierarchical objects are exposed by nested paths.
 * <p>
 * For example:
 * <pre class="code">
 * &nbsp; {
 * &nbsp;  "teams": {
 * &nbsp;    "ferrari": {
 * &nbsp;      "engine": "ferrari",
 * &nbsp;      "score": 522,
 * &nbsp;      "private": false
 * &nbsp;     },
 * &nbsp;     "williams": {
 * &nbsp;       "engine": "mercedes",
 * &nbsp;       "score": 83,
 * &nbsp;       "private": true
 * &nbsp;     }
 * &nbsp;   }
 * &nbsp; }
 * </pre>
 * is transformed into these properties:
 * <pre class="code">
 * teams.ferrari.engine=ferrari
 * teams.ferrari.score=522
 * teams.ferrari.private=false
 * teams.williams.engine=mercedes
 * teams.williams.score=83
 * teams.williams.private=true
 * </pre>
 * {@code []} brackets are used to split lists into property keys.
 * <p>
 * For example:
 * <pre class="code">
 * &nbsp; {
 * &nbsp;   "months": {
 * &nbsp;     "odd": [
 * &nbsp;       "January",
 * &nbsp;       "March",
 * &nbsp;       "May",
 * &nbsp;       "July",
 * &nbsp;       "August",
 * &nbsp;       "October",
 * &nbsp;       "December"
 * &nbsp;     ],
 * &nbsp;     "even": [
 * &nbsp;       "April",
 * &nbsp;       "June",
 * &nbsp;       "September",
 * &nbsp;       "November"
 * &nbsp;     ],
 * &nbsp;     "both": "February"
 * &nbsp;   }
 * &nbsp; }
 * </pre>
 * is transformed into these properties:
 * <pre class="code">
 * months.odd[0]=January
 * months.odd[1]=March
 * months.odd[2]=May
 * months.odd[3]=July
 * months.odd[4]=August
 * months.odd[5]=October
 * months.odd[6]=December
 * months.even[0]=April
 * months.even[1]=June
 * months.even[2]=September
 * months.even[3]=November
 * months.both=February
 * </pre>
 */
public class JsonConfigurationSource implements IterableConfigurationSource {
    /**
     * Property name used when JSON does not represent map.
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> properties;

    /**
     * Constructs value source from {@link InputStream}.
     * <p>
     * <em>Note:</em> JSON is loaded and processed in the constructor and {@code inputStream} is closed
     * at the end of processing.
     *
     * @param inputStream input stream which provides JSON.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    public JsonConfigurationSource(InputStream inputStream) {
        requireNonNull(inputStream, "inputStream cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object object = objectReader.readValue(inputStream);
            this.properties = createProperties(object);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to process JSON.", e);
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
     * <em>Note:</em> JSON is loaded and processed in the constructor and {@code inputStream} is closed
     * at the end of processing.
     *
     * @param reader reader which provides JSON source.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    public JsonConfigurationSource(Reader reader) {
        requireNonNull(reader, "reader cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object object = objectReader.readValue(reader);
            this.properties = createProperties(object);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to process YAML.", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to close inputStream.", e);
            }
        }
    }

    /**
     * Constructs value source from {@link File}.
     *
     * @param file file which contains JSON source.
     * @throws IllegalArgumentException when provided file not found.
     * @throws UncheckedIOException     when {@link IOException} is thrown during processing.
     */
    public JsonConfigurationSource(File file) {
        requireNonNull(file, "file cannot be null");

        try {
            ObjectReader objectReader = objectMapper.readerFor(Object.class);
            Object object = objectReader.readValue(file);
            this.properties = createProperties(object);
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

    private Map<String, String> createProperties(Object object) {
        Map<String, String> result = new LinkedHashMap<>();
        buildFlattenedMap(result, createMap(object), null);
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
