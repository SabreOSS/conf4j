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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.Objects.requireNonNull;

/**
 * This class converts {@link Pattern} to/from string.
 */
public class PatternConverter implements TypeConverter<Pattern> {

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> && Pattern.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public Pattern fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        try {
            return (value == null) ? null : Pattern.compile(value);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Unable to convert to a Pattern: " + value, e);
        }
    }

    @Override
    public String toString(Type type, Pattern value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return Objects.toString(value, null);
    }
}
