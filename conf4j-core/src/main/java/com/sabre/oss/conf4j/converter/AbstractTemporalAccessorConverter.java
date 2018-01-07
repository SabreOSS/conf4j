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

package com.sabre.oss.conf4j.converter;

import com.sabre.oss.conf4j.internal.utils.spring.ConcurrentReferenceHashMap;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * This is base class for converting date-time values from/to string.
 * <p>
 * The converter supports {@value #FORMAT} meta-attribute which specifies the format of resulting string representation.
 * The format string must be compatible with the format defined by {@link DateTimeFormatter}.
 * <p>
 * While converting from value to string if the format is not provided,
 * appropriate ISO representation is used (for example {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME})
 *
 * @param <T> actual temporal accessor type
 */

public abstract class AbstractTemporalAccessorConverter<T extends TemporalAccessor> implements TypeConverter<T> {
    /**
     * Format attribute name.
     */
    public static final String FORMAT = "format";

    private static final ConcurrentMap<String, DateTimeFormatter> cache = new ConcurrentReferenceHashMap<>();

    protected abstract T parse(String value, DateTimeFormatter formatterForPattern);

    protected abstract DateTimeFormatter getDefaultFormatter();

    /**
     * Converts String to {@code T}.
     *
     * @param type       actual type definition.
     * @param value      string representation of the value which is converted to a given type.
     *                   In case it is {@code null}, the converter should return {@code null}.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, the value for {@value #FORMAT} key will be used during conversion
     *                   as a formatting pattern.
     * @return value converted to {@code T}
     * @throws IllegalArgumentException when {@code value} cannot be converted to {{@code T} because of
     *                                  invalid format of {@code value} string or invalid formatting pattern.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public T fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        String format = (attributes == null) ? null : attributes.get(FORMAT);
        try {
            return parse(value, getFormatterForPattern(format));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(format("Unable to convert to %s: %s. " +
                    "The value doesn't match specified format %s.", getSimpleClassName(type), value, format), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(format("Unable to convert to %s: %s. " +
                    "Invalid format: '%s'", getSimpleClassName(type), value, format), e);
        }
    }

    /**
     * Converts value from {@code T} to String.
     *
     * @param type       actual type.
     * @param value      value that needs to be converted to string.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, the value for {@value #FORMAT} key will be used during conversion
     *                   as a formatting pattern.
     * @return string representation of the {@code value}.
     * @throws IllegalArgumentException when {@code value} cannot be converted to String because of
     *                                  invalid formatting pattern or error during printing.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public String toString(Type type, T value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        String format = (attributes == null) ? null : attributes.get(FORMAT);
        try {
            return format == null ? value.toString() : getFormatterForPattern(format).format(value);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(format("Unable to convert %s to String. ", getSimpleClassName(type)) +
                    "Error occurred during printing.", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(format("Unable to convert %s to String. " +
                    "Invalid format: '%s'", getSimpleClassName(type), format), e);
        }
    }

    DateTimeFormatter getFormatterForPattern(String pattern) {
        return pattern == null ? getDefaultFormatter() : cache.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }

    String getSimpleClassName(Type type) {
        return ((Class<?>) type).getSimpleName();
    }
}
