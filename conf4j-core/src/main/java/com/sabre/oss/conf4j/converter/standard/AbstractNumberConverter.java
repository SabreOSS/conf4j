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
import com.sabre.oss.conf4j.internal.utils.spring.ConcurrentReferenceHashMap;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * This is base class for converting numeric values from/to string.
 * <p>
 * The converter supports {@value #FORMAT} meta-attribute which specifies the format of resulting string representation.
 * The format string must be compatible with the format defined by {@link DecimalFormat}.
 * <p>
 * In case the format is not provided, <em>natural</em> number representation is used:
 * {@link Objects#toString()} while converting from value to string and <em>NumberType.valueOf(String value)</em>
 * while converting from string to a type value (<em>NumberType</em> is a concrete class for example {@code Double}).
 * <p>
 * The converter also supports {@value LOCALE} meta-attribute which specifies
 * the locale used during conversion. It is used only when {@value FORMAT} attribute is provided.
 * The locale must be a ISO 639. If not specified, {@link Locale#US} locale is used.
 *
 * @param <T> actual number type
 */
public abstract class AbstractNumberConverter<T extends Number> implements TypeConverter<T> {

    /**
     * Format meat-attribute name.
     */
    public static final String FORMAT = "format";

    /**
     * Locale meta-attribute name.
     */
    public static final String LOCALE = "locale";

    private static final ConcurrentMap<CacheKey, DecimalFormat> formatCache = new ConcurrentReferenceHashMap<>();

    protected abstract T parseWithoutFormat(String value);

    protected abstract T convertResult(Number value);

    protected boolean isApplicable(Type type, Class<T> clazz, Class<T> primitiveType) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> &&
                (clazz.isAssignableFrom((Class<?>) type) || primitiveType != null && primitiveType.isAssignableFrom((Class<?>) type));
    }

    /**
     * Converts String to given type
     *
     * @param type       actual type.
     * @param value      string representation of the value which is converted to a given type.
     *                   In case it is {@code null}, the converter returns {@code null}.
     * @param attributes meta-attributes; see class javadoc for more details.
     * @return value converted to {@code T}
     * @throws IllegalArgumentException when {@code value} cannot be converted to {@code T} because of
     *                                  invalid format of {@code value} string, invalid formatting pattern or
     *                                  value out of given type range.
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
            if (format == null) {
                return parseWithoutFormat(value);
            } else {
                String locale = attributes.get(LOCALE);
                return convertResult(parseWithFormat(value, format, locale, type));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(format("Unable to convert to %s: %s", getSimpleClassName(type), value), e);
        }
    }

    /**
     * Converts value from {@code T} to String
     *
     * @param type       actual type.
     * @param value      value that needs to be converted to string.
     * @param attributes meta-attributes; see class javadoc for more details.
     * @return string representation of the {@code value}
     * @throws IllegalArgumentException when {@code value} cannot be converted to string because of
     *                                  invalid formatting pattern or error during printing.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public String toString(Type type, T value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        String format = (attributes == null) ? null : attributes.get(FORMAT);
        if (format == null) {
            return Objects.toString(value, null);
        } else {
            String formattingPattern = attributes.get(FORMAT);
            String locale = attributes.get(LOCALE);
            return getFormatter(formattingPattern, locale).format(value);
        }
    }

    protected Number parseWithFormat(String value, String format, String locale, Type type) {
        NumberFormat formatter = getFormatter(format, locale);

        try {
            return formatter.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException(format("Unable to convert to %s. " +
                    "The value doesn't match specified format: %s", getSimpleClassName(type), format), e);
        }
    }

    protected DecimalFormat getFormatter(String format, String locale) {
        CacheKey attributes = new CacheKey(format, locale);
        // DecimalFormat is not thread safe, clone() creates a copy efficiently
        return (DecimalFormat) formatCache.computeIfAbsent(attributes, this::createFormatter).clone();
    }

    private DecimalFormat createFormatter(CacheKey attributes) {
        Locale formatterLocale = attributes.locale == null ? Locale.US : Locale.forLanguageTag(attributes.locale);
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(formatterLocale);
        formatter.applyPattern(attributes.format);

        return formatter;
    }

    private String getSimpleClassName(Type type) {
        return ((Class<?>) type).getSimpleName();
    }

    private static final class CacheKey {
        private final String format;
        private final String locale;

        CacheKey(String format, String locale) {
            this.format = format;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(format, cacheKey.format) &&
                    Objects.equals(locale, cacheKey.locale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(format, locale);
        }
    }
}
