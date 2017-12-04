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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import static java.lang.Short.MAX_VALUE;
import static java.lang.Short.MIN_VALUE;
import static java.util.Objects.requireNonNull;

/**
 * This class converts {@link Short} to/from string.
 * <p>
 * The converter supports {@value #FORMAT} attribute (provided in the attributes map) which specifies
 * the format used during conversion. The format is compliant with {@link java.text.DecimalFormat}
 * </p>
 * <p>
 * When the format is not specified, {@link Objects#toString() } method is used.
 * </p>
 * <p>
 * The converter supports also {@value LOCALE} attribute (provided in the attributes map) which specifies
 * the locale used during conversion. It will be used only if {@value FORMAT} attribute is provided.
 * The locale should be provided as ISO 639 string. If not present, Locale.US is used.
 * </p>
 */
public class ShortTypeConverter extends AbstractNumericConverter<Short> {

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> &&
                (Short.class.isAssignableFrom((Class<?>) type) || Short.TYPE.isAssignableFrom((Class<?>) type));
    }

    @Override
    protected Short parseWithoutFormat(String value) {
        return Short.valueOf(value);
    }

    @Override
    protected Short convertResult(Number value) {
        if (value.longValue() > MAX_VALUE || value.longValue() < MIN_VALUE) {
            throw new IllegalArgumentException(String.format("Provided value: %d is out of Short type range.",
                    value.longValue()));
        }
        return value.shortValue();
    }
}
