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

package com.sabre.oss.conf4j.converter.standard;

import com.sabre.oss.conf4j.converter.TypeConverter;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * This class converts {@link Enum} of any type to/from string.
 * <p>
 * {@link Enum#name()} method is used for converting an enumeration value to a string in {@link #toString(Type, Enum, Map)}.
 * This method is also used by {@link #fromString(Type, String, Map)} to find matching enumeration values.
 * </p>
 */
public class EnumTypeConverter implements TypeConverter<Enum<?>> {

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> && Enum.class.isAssignableFrom((Class<?>) type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enum<?> fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return value == null ? null : toEnumValue((Class<Enum<?>>) type, value);
    }

    @Override
    public String toString(Type type, Enum<?> value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return value == null ? null : value.name();
    }

    protected Enum<?> toEnumValue(Class<Enum<?>> enumClass, String value) {
        for (Enum<?> t : enumClass.getEnumConstants()) {
            if (t.name().equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException(
                format("Unable to convert a value to an enumeration: %s. The value for %s enumeration type must be one of: %s",
                        value, enumClass.getName(), Arrays.toString(enumClass.getEnumConstants())));
    }
}
