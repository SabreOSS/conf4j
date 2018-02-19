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

package com.sabre.oss.conf4j.converter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.Validate.noNullElements;

/**
 * {@code ChainedTypeConverter} is a {@link TypeConverter} that supports conversion for multiple types.
 * This converter delegates to the chain of converters to perform conversions.
 */
public class ChainedTypeConverter implements TypeConverter<Object> {
    private final List<TypeConverter<?>> converters;
    private final Map<Key, TypeConverter<Object>> typeToConverter;

    /**
     * Create a converter using {@code converters}.
     *
     * @param converters list of converters.
     * @throws NullPointerException when {@code converters} is {@code null}.
     * @throws IllegalAccessError   when {@code converters} contains at least one {@code null}.
     */
    public ChainedTypeConverter(List<TypeConverter<?>> converters) {
        this.converters = requireNonNull(converters, "converters cannot be null");
        noNullElements(converters, "converters list has null element at index: %d");

        this.typeToConverter = new ConcurrentHashMap<>();
    }

    /**
     * Create a converter using {@code converters}.
     *
     * @param converters array of converters.
     * @throws NullPointerException when {@code converters} is {@code null}.
     * @throws IllegalAccessError   when {@code converters} contains at least one {@code null}.
     */
    public ChainedTypeConverter(TypeConverter<?>... converters) {
        this(asList(converters));
    }

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return converterFor(type, attributes, false) != null;
    }

    @Override
    public Object fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return converterFor(type, attributes).fromString(type, value, attributes);
    }

    @Override
    public String toString(Type type, Object value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return converterFor(type, attributes).toString(type, value, attributes);
    }

    private TypeConverter<Object> converterFor(Type type, Map<String, String> attributes) {
        return converterFor(type, attributes, true);
    }

    @SuppressWarnings("unchecked")
    private TypeConverter<Object> converterFor(Type type, Map<String, String> attributes, boolean exceptionIfNotFound) {
        TypeConverter<Object> typeConverter = typeToConverter.computeIfAbsent(new Key(type, attributes), (k) -> {
            for (TypeConverter<?> converter : converters) {
                if (converter.isApplicable(k.type, k.attributes)) {
                    return (TypeConverter<Object>) converter;
                }
            }
            return null;
        });

        if (typeConverter != null || !exceptionIfNotFound) {
            return typeConverter;
        }

        throw new IllegalArgumentException("Don't know how to convert " + type);
    }

    private static final class Key {
        private final Type type;
        private final Map<String, String> attributes;

        private Key(Type type, Map<String, String> attributes) {
            this.type = type;
            this.attributes = attributes;
        }

        public Type getType() {
            return type;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;

            return Objects.equals(type, key.type) &&
                    Objects.equals(attributes, key.attributes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, attributes);
        }

        @Override
        public String toString() {
            return type + ": " + attributes;
        }
    }
}
