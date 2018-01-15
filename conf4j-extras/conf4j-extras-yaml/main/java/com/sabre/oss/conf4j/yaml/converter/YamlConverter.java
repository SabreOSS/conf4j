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

package com.sabre.oss.conf4j.yaml.converter;

import com.sabre.oss.conf4j.converter.TypeConverter;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import static com.sabre.oss.conf4j.yaml.converter.Yaml.CONVERTER;
import static com.sabre.oss.conf4j.yaml.converter.Yaml.YAML;
import static java.util.Objects.requireNonNull;

/**
 * Type converter which supports object conversion to/from YAML.
 * <p>
 * If the converter ignores converter attribute, it is always applied.
 * Otherwise {@link com.sabre.oss.conf4j.yaml.converter.Yaml#CONVERTER} meta-attribute with
 * {@link com.sabre.oss.conf4j.yaml.converter.Yaml#YAML} value must be provided.
 *
 * @see com.sabre.oss.conf4j.yaml.converter.Yaml
 */
public class YamlConverter<T> implements TypeConverter<T> {
    private final boolean ignoreConverterAttribute;

    /**
     * Create YamlConverter instance. By default the converter is not ignoring
     * {@link com.sabre.oss.conf4j.yaml.converter.Yaml#CONVERTER} attribute. See class javadoc for more details.
     */
    public YamlConverter() {
        this(false);
    }

    /**
     * Create YamlConverter instance.
     *
     * @param ignoreConverterAttribute flag indicating whether {@link com.sabre.oss.conf4j.yaml.converter.Yaml#CONVERTER}
     *                                 should be ignored. See class javadoc for more details.
     */
    public YamlConverter(boolean ignoreConverterAttribute) {
        this.ignoreConverterAttribute = ignoreConverterAttribute;
    }

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        String converter = (attributes == null)
                ? null
                : attributes.get(CONVERTER);
        return ignoreConverterAttribute || Objects.equals(converter, YAML);
    }

    @Override
    public T fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        Yaml yaml = new Yaml();
        return yaml.loadAs(value, (Class<T>) type);
    }

    @Override
    public String toString(Type type, T value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        Yaml yaml = new Yaml();
        return yaml.dumpAsMap(value);
    }
}
