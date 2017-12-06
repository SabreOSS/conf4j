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
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

/**
 * This class converts {@link String} to/from string.
 * <p>
 * This converter can escapes/unescapes special characters like new lines or tabs using the same rules as java.
 * It is controlled via {@code escape} constructor parameter but can be change by {@value #ESCAPE} meta-attribute
 * during conversion. When {@value #ESCAPE} is {@value #TRUE}, then resulting string will be escaped,
 * when it is {@value FALSE}, not string transformation is performed.
 */
public class StringConverter implements TypeConverter<String> {

    /**
     * Escape string meta-attribute name.
     */
    public static final String ESCAPE = "escape";

    private static final String FALSE = "false";
    private static final String TRUE = "true";

    private final boolean escape;

    /**
     * Create the converter instance with specified {@code escape} parameter.
     *
     * @param escape when {@code true}, string special characters are escaped using <em>java</em> rules.
     */
    public StringConverter(boolean escape) {
        this.escape = escape;
    }

    /**
     * Create the converter instance which escapes special characters using <em>java</em> escaping rules.
     */
    public StringConverter() {
        this(true);
    }

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> && String.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public String fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return shouldEscape(attributes) ? unescapeJava(value) : value;
    }

    @Override
    public String toString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return shouldEscape(attributes) ? escapeJava(value) : value;
    }

    private boolean shouldEscape(Map<String, String> attributes) {
        if (attributes == null || !attributes.containsKey(ESCAPE)) {
            return escape;
        } else {
            String shouldEscape = attributes.get(ESCAPE);
            if (Objects.equals(shouldEscape, TRUE)) {
                return true;
            } else if (Objects.equals(shouldEscape, FALSE)) {
                return false;
            } else {
                throw new IllegalArgumentException(format(
                        "Invalid '%s' meta-attribute value, it must be either '%s' or '%s', but '%s' is provided.",
                        ESCAPE, TRUE, FALSE, shouldEscape));
            }
        }
    }
}
