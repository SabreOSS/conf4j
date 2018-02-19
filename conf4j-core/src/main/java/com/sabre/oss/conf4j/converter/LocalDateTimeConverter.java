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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Objects.requireNonNull;

/**
 * This class converts {@link LocalDateTime} to/from string.
 * <p>
 * The converter supports {@value #FORMAT} attribute (provided in the attributes map) which specifies
 * the format used during conversion. The format is compliant with {@link DateTimeFormatter}.
 * <p>
 * When the format is not specified, {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} is used.
 * <p>
 * For more details see {@link AbstractTemporalAccessorConverter}
 */
public class LocalDateTimeConverter extends AbstractTemporalAccessorConverter<LocalDateTime> {

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> && LocalDateTime.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    protected LocalDateTime parse(String value, DateTimeFormatter formatterForPattern) {
        return LocalDateTime.parse(value, formatterForPattern);
    }

    @Override
    protected DateTimeFormatter getDefaultFormatter() {
        return ISO_LOCAL_DATE_TIME;
    }
}
