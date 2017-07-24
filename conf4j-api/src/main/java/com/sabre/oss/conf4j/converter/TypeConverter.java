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
package com.sabre.oss.conf4j.converter;

import java.lang.reflect.Type;

/**
 * Adapts string value to given type.
 * <p>
 * Converter must be <b>thread safe</b>, <b>symmetric</b> and <b>stateless</b>.
 * It should provide immutable values, wherever possible, because values may be cached and reused.
 * <p>
 *
 * @param <T> The type that this type converter handles.
 */
public interface TypeConverter<T> {

    /**
     * Check if the type converter is applicable for type {@code type}.
     *
     * @param type actual type definition.
     * @return {@code true} when this type converter is applicable for a given type definition.
     * @throws NullPointerException when {@code type} is {@code null}.
     */
    boolean isApplicable(Type type);

    /**
     * Converts String to the target type.
     *
     * @param type  actual type definition.
     * @param value string representation of the value which is converted to {@code T}.
     *              In case it is {@code null}, the converter should return either {@code null} or a value
     *              that is equivalent (for example an empty list).
     * @return value converted to type {@code T}.
     * @throws IllegalArgumentException when {@code value} cannot be converted to {@code T}.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    T fromString(Type type, String value);

    /**
     * Converts value from target type to String.
     *
     * @param type  actual type definition.
     * @param value value that needs to be converted to string.
     * @return string representation of the {@code value}.
     * @throws IllegalArgumentException {@code value} cannot be converted to string.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    String toString(Type type, T value);
}
