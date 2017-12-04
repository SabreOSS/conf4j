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

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

/**
 * This class converts {@link String} to/from string.
 * <p>
 * This converter escapes/unescapes special characters like new lines or tabs using the same rules as java.
 * When "escape" attribute name is provided and is equal to "false", escaping/unescaping is not performed.
 * </p>
 */
public class EscapingStringTypeConverter implements TypeConverter<String> {

    /**
     * Escape string attribute name.
     */
    public static final String ESCAPE = "escape";

    private static final String FALSE = "false";
    private static final String TRUE = "true";

    private boolean escape;

    /**
     * Create the converter instance with specified escape argument.
     *
     * @param escape Sets if values provided to converter should be escaped/unescaped
     */
    public EscapingStringTypeConverter(boolean escape) {
        this.escape = escape;
    }

    /**
     * Create the converter instance with values always escaped/unescaped.
     */
    public EscapingStringTypeConverter() {
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

        if (attributes != null && attributes.containsKey(ESCAPE)) {
            String escape = attributes.get(ESCAPE);

            if (escape.equalsIgnoreCase(FALSE)) {
                return value;
            }
            if (escape.equalsIgnoreCase(TRUE)) {
                return unescapeJava(value);
            }
            throw new IllegalArgumentException(String.format(
                    "Value for escape attribute should be equal to true or false. Provided: %s", escape));
        }

        return escape ? unescapeJava(value) : value;
    }

    @Override
    public String toString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (attributes != null && attributes.containsKey(ESCAPE)) {
            String escape = attributes.get(ESCAPE);

            if (escape.equalsIgnoreCase(FALSE)) {
                return value;
            }
            if (escape.equalsIgnoreCase(TRUE)) {
                return escapeJava(value);
            }
            throw new IllegalArgumentException(String.format(
                    "Value for escape attribute should be equal to true or false. Provided: %s", escape));
        }

        return escape ? escapeJava(value) : value;
    }
}
