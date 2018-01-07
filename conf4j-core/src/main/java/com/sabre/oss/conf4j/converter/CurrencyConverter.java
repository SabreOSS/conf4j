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
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.forLanguageTag;
import static java.util.Objects.requireNonNull;

/**
 * This class converts {@link Currency} to/from string.
 */
public class CurrencyConverter implements TypeConverter<Currency> {

    /**
     * Locale meta-attribute name.
     */
    public static final String LOCALE = "locale";

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> && Currency.class.isAssignableFrom((Class<?>) type);
    }

    /**
     * Converts String to {@link Currency}.
     *
     * @param type       actual type.
     * @param value      string representation of the value which is converted to {@link Currency}.
     *                   In case it is {@code null}, the converter returns {@code null}.
     * @param attributes additional meta-data attributes. It can be {@code null}.
     * @return value converted to {@link Currency}
     * @throws IllegalArgumentException when {@code value} cannot be converted to {@link Currency} because of
     *                                  invalid format of {@code value} string.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public Currency fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return value == null
                ? null
                : Currency.getInstance(value);
    }

    /**
     * Converts value from {@link Currency} to String
     *
     * @param type       actual type.
     * @param value      value that needs to be converted to string.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, the value for {@value #LOCALE} key will be used as locale during conversion.
     *                   The locale must be a ISO 639. If not specified or invalid, {@link Locale#US} locale is used.
     * @return string representation of the {@code value}
     * @throws NullPointerException when {@code type} is {@code null}.
     */
    @Override
    public String toString(Type type, Currency value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        Locale locale = attributes != null && attributes.containsKey(LOCALE)
                ? forLanguageTag(attributes.get(LOCALE))
                : Locale.US;
        return value.getDisplayName(locale);
    }
}
