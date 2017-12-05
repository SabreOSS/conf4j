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
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * <p>This class converts {@link Boolean} to/from string.</p>
 * <p>
 * The converter supports {@value #FORMAT} meta-attribute which specifies the values corresponding to
 * {@code true} and {@code false} values.
 * </p>
 * <p>
 * The format of {@value #FORMAT} meta-attribute value is: {@code {true-value}/{false-value}}
 * where {@code {true-value}} is as string which is used when the boolean value is {@code true}
 * and {@code {false-value}} is as string which is used when the boolean value is {@code false}.
 * </p>
 * <p>
 * For example: {@code yes/no}, {@code true/false}
 * </p>
 * When the format is not specified {@value #TRUE} and {@value #FALSE} values are used.
 */
public class BooleanTypeConverter implements TypeConverter<Boolean> {
    /**
     * Format attribute name.
     */
    public static final String FORMAT = "format";

    private static final char SEPARATOR = '/';
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final ConcurrentMap<String, Pair<String, String>> cache = new ConcurrentReferenceHashMap<>();


    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> &&
                (Boolean.class.isAssignableFrom((Class<?>) type) || Boolean.TYPE.isAssignableFrom((Class<?>) type));
    }

    /**
     * Converts String to {@link Boolean}
     *
     * @param type       actual type definition.
     * @param value      string representation of the value which is converted to {@link Boolean}.
     *                   In case it is {@code null}, the converter should return either {@code null} or a value
     *                   that is equivalent (for example an empty list).
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, the value for {@value #FORMAT} key will be used during conversion
     *                   as a formatting pattern.
     * @return value converted to {@link Boolean}
     * @throws IllegalArgumentException when {@code value} cannot be converted to {@link Boolean} because of
     *                                  invalid format of {@code value} string or invalid formatting pattern.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public Boolean fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        String format = (attributes == null) ? null : attributes.get(FORMAT);
        if (format != null) {
            Pair<String, String> values = cache.computeIfAbsent(format, this::getValues);
            if (value.equals(values.getLeft())) {
                return Boolean.TRUE;
            }
            if (value.equals(values.getRight())) {
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException(
                    format("Unable to convert to Boolean, values must be either '%s' or '%s' but provided value is '%s'.",
                            values.getLeft(), values.getRight(), value));
        } else {
            if (TRUE.equals(value)) {
                return Boolean.TRUE;
            }
            if (FALSE.equals(value)) {
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException(format("Unable to convert to Boolean. Unknown value: %s", value));
        }
    }

    /**
     * Converts value from {@link Boolean} to String.
     *
     * @param type       actual type definition.
     * @param value      value that needs to be converted to string.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, value for "format" key will be used as formatting pattern.
     * @return string representation of the {@code value}
     * @throws IllegalArgumentException when {@code value} cannot be converted to String because of
     *                                  invalid formatting pattern or error during printing.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public String toString(Type type, Boolean value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (attributes != null && attributes.containsKey(FORMAT)) {
            String format = attributes.get(FORMAT);
            Pair<String, String> values = cache.computeIfAbsent(format, this::getValues);
            return value ? values.getLeft() : values.getRight();
        } else {
            return Objects.toString(value, null);
        }
    }

    private Pair<String, String> getValues(String format) {
        int idx = format.indexOf(SEPARATOR);
        if (idx < 0) {
            throw new IllegalArgumentException(format(
                    "Invalid '%s' meta-attribute value, it must contain '%c' separator character. Provided value is '%s'.",
                    FORMAT, SEPARATOR, format));
        }

        return Pair.of(format.substring(0, idx), (idx == format.length() - 1) ? EMPTY : format.substring(idx + 1));
    }

}
