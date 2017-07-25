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

package com.sabre.oss.conf4j.converter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
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
    private final Map<Type, TypeConverter<Object>> typeToConverter;

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
    public boolean isApplicable(Type type) {
        requireNonNull(type, "type cannot be null");

        return converterFor(type, false) != null;
    }

    @Override
    public Object fromString(Type type, String value) {
        requireNonNull(type, "type cannot be null");

        return converterFor(type).fromString(type, value);
    }

    @Override
    public String toString(Type type, Object value) {
        requireNonNull(type, "type cannot be null");

        return converterFor(type).toString(type, value);
    }

    private TypeConverter<Object> converterFor(Type type) {
        return converterFor(type, true);
    }

    @SuppressWarnings("unchecked")
    private TypeConverter<Object> converterFor(Type type, boolean exceptionIfNotFound) {
        TypeConverter<Object> typeConverter = typeToConverter.computeIfAbsent(type, (t) -> {
            for (TypeConverter<?> converter : converters) {
                if (converter.isApplicable(type)) {
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
}
