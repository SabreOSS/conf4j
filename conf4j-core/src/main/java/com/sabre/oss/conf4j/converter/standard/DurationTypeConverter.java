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
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * This class converts {@link Duration} to/from string.
 * <p>
 * The converter supports {@value #FORMAT} attribute (provided in the attributes map) which specifies
 * the format used during conversion. The format is compliant with {@link DurationFormatUtils}.
 * </p>
 * <p>
 * When the format is not specified, {@link Objects#toString() } method is used.
 * </p>
 */
public class DurationTypeConverter implements TypeConverter<Duration> {

    /**
     * Format attribute name.
     */
    public static final String FORMAT = "format";

    private static final ConcurrentMap<String, SimpleDateFormat> cache = new ConcurrentReferenceHashMap<>();

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> && Duration.class.isAssignableFrom((Class<?>) type);
    }

    /**
     * Converts String to {@link Duration}.
     *
     * @param type       actual type definition.
     * @param value      string representation of the value which is converted to {@link Duration}.
     *                   In case it is {@code null}, the converter should return either {@code null} or a value
     *                   that is equivalent (for example an empty list).
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, the value for {@value #FORMAT} key will be used during conversion
     *                   as a formatting pattern.
     * @return value converted to {@link Duration}
     * @throws IllegalArgumentException when {@code value} cannot be converted to {@link Duration} because of
     *                                  invalid format of {@code value} string or invalid formatting pattern.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public Duration fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (attributes != null && attributes.containsKey(FORMAT)) {
            String formattingPattern = attributes.get(FORMAT);
            try {
                SimpleDateFormat sdf = cache.computeIfAbsent(formattingPattern, SimpleDateFormat::new);
                LocalDateTime ldt = LocalDateTime.ofInstant(sdf.parse(value).toInstant(), ZoneId.systemDefault());
                return Duration.between(Instant.EPOCH, ldt.toInstant(ZoneOffset.ofTotalSeconds(0)));
            } catch (ParseException e) {
                throw new IllegalArgumentException(format("Unable to convert to Duration: %s. " +
                        "The value doesn't match specified format %s.", value, formattingPattern), e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(format("Unable to convert to Duration: %s. " +
                        "Invalid format: %s", value, formattingPattern), e);
            }
        }

        try {
            return (value == null) ? null : Duration.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(format("Unable to convert to Duration: %s", value), e);
        }
    }

    /**
     * Converts value from {@link Duration} to String.
     *
     * @param type       actual type definition.
     * @param value      value that needs to be converted to string.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, value for "format" key will be used as formatting pattern.
     * @return string representation of the {@code value}.
     * @throws IllegalArgumentException when {@code value} cannot be converted to String because of
     *                                  invalid formatting pattern or error during printing.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public String toString(Type type, Duration value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        if (attributes != null && attributes.containsKey("format")) {
            String formattingPattern = attributes.get(FORMAT);
            try {
                return DurationFormatUtils.formatDuration(value.toMillis(), formattingPattern);
            } catch (DateTimeException e) {
                throw new IllegalArgumentException(
                        "Unable to convert Duration to String. Error occurred during printing.", e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(format(
                        "Unable to convert Duration to String. Invalid duration value: %s", value), e);
            }
        }

        return value.toString();
    }

}
