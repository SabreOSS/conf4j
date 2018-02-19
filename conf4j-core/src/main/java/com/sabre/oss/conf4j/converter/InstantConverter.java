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
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Objects.requireNonNull;

/**
 * This class converts {@link Instant} to/from string.
 * <p>
 * The converter supports {@value #ZONE} attribute (provided in the attributes map) which specifies
 * the zone used in conversion.The zone is compliant with {@link ZoneId}.
 * When attribute is not provided default zone value is used(America/Chicago).
 * <p>
 * When the format is not specified, {@link DateTimeFormatter#ISO_INSTANT} is used.
 * <p>
 * For more details see {@link AbstractTemporalAccessorConverter}
 */
public class InstantConverter extends AbstractTemporalAccessorConverter<Instant> {
    /**
     * Zone attribute name.
     */
    public static final String ZONE = "zone";

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Chicago");

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> && Instant.class.isAssignableFrom((Class<?>) type);
    }

    /**
     * Converts value from {@link Instant} to String.
     *
     * @param type       actual type definition.
     * @param value      value that needs to be converted to string.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, value for {@value #FORMAT} key will be used as formatting pattern.
     *                   Value for {@value #ZONE} key will be used as adjustment time for different localization,
     *                   if not specified or invalid default zone will be used.
     * @return string representation of the {@code value}.
     * @throws IllegalArgumentException when {@code value} cannot be converted to String because of
     *                                  invalid formatting pattern or error during printing.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public String toString(Type type, Instant value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }
        String format = null;
        ZoneId zoneId = DEFAULT_ZONE;
        if (attributes != null) {
            format = attributes.get(FORMAT);
            String zone = attributes.get(ZONE);
            if (ZoneId.getAvailableZoneIds().contains(zone)) {
                zoneId = ZoneId.of(zone);
            }
        }

        try {
            return format == null ? value.toString() : getFormatterForPattern(format).withZone(zoneId).format(value);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(format("Unable to convert %s to String. ", getSimpleClassName(type)) +
                    "Error occurred during printing.", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(format("Unable to convert %s to String. " +
                    "Invalid format: '%s'", getSimpleClassName(type), format), e);
        }
    }

    @Override
    protected Instant parse(String value, DateTimeFormatter formatterForPattern) {
        return Instant.from(formatterForPattern.parse(value));
    }

    @Override
    protected DateTimeFormatter getDefaultFormatter() {
        return ISO_INSTANT;
    }
}
