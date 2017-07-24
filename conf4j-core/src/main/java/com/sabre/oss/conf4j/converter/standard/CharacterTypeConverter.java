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

package com.sabre.oss.conf4j.converter.standard;

import com.sabre.oss.conf4j.converter.TypeConverter;

import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

/**
 * This class converts {@link Character} to/from string.
 */
public class CharacterTypeConverter implements TypeConverter<Character> {
    @Override
    public boolean isApplicable(Type type) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> &&
                (Character.class.isAssignableFrom((Class<?>) type) || Character.TYPE.isAssignableFrom((Class<?>) type));
    }

    @Override
    public Character fromString(Type type, String value) {
        requireNonNull(type, "type cannot be null");

        if (value == null || value.isEmpty()) {
            return null;
        }

        if (value.length() == 1) {
            return value.charAt(0);
        }

        if (value.charAt(0) == '\\') {
            String unescapedValue;
            try {
                unescapedValue = unescapeJava(value);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Unable to convert to a Character: " + value, e);
            }
            if (unescapedValue.length() == 1) {
                return unescapedValue.charAt(0);
            }
        }

        throw new IllegalArgumentException("Unable to convert to a Character: " + value);

    }

    @Override
    public String toString(Type type, Character value) {
        requireNonNull(type, "type cannot be null");

        return value == null ? null : escapeJava(value.toString());
    }
}
