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
import java.util.Currency;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * This class converts {@link Currency} to/from string.
 */
public class CurrencyConverter implements TypeConverter<Currency> {
    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> && Currency.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public Currency fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return value == null
                ? null
                : Currency.getInstance(value);
    }

    @Override
    public String toString(Type type, Currency value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return value == null
                ? null
                : value.getCurrencyCode();
    }
}
