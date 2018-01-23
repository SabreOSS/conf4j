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

package com.sabre.oss.conf4j.json.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sabre.oss.conf4j.converter.TypeConverter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import static com.sabre.oss.conf4j.json.converter.Json.CONVERTER;
import static com.sabre.oss.conf4j.json.converter.Json.JSON;
import static java.util.Objects.requireNonNull;

/**
 * Type converter which supports object conversion to/from JSON.
 *
 * @see com.sabre.oss.conf4j.json.converter.Json
 */
public class JsonConverter<T> implements TypeConverter<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final boolean ignoreConverterAttribute;

    /**
     * Creates JsonConverter instance which is applied to the properties which has {@value com.sabre.oss.conf4j.json.converter.Json#CONVERTER}
     * meta-attribute with value {@value com.sabre.oss.conf4j.json.converter.Json#JSON} assigned.
     *
     * @see com.sabre.oss.conf4j.json.converter.Json
     */
    public JsonConverter() {
        this(false);
    }

    /**
     * Creates JsonConverter instance.
     *
     * @param ignoreConverterAttribute flag indicating whether {@value com.sabre.oss.conf4j.json.converter.Json#CONVERTER}
     *                                 meta-attribute should be ignored (when {@code true}) or checked (when {@code false}).
     * @see com.sabre.oss.conf4j.json.converter.Json
     */
    public JsonConverter(boolean ignoreConverterAttribute) {
        this.ignoreConverterAttribute = ignoreConverterAttribute;
    }

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        String converter = (attributes == null)
                ? null
                : attributes.get(CONVERTER);
        return ignoreConverterAttribute || Objects.equals(converter, JSON);
    }

    @Override
    public T fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        try {
            ObjectReader objectReader = objectMapper.readerFor((Class<T>) type);
            return objectReader.readValue(value);
        } catch (IOException e) {
            throw new AssertionError("An IOException occurred when this was assumed to be impossible.", e);
        }
    }

    @Override
    public String toString(Type type, T value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        try {
            ObjectWriter objectWriter = objectMapper.writer();
            return objectWriter.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Unable to process JSON.", e);
        }
    }
}
