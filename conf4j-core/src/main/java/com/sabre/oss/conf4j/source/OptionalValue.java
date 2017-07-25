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

package com.sabre.oss.conf4j.source;

import java.util.Objects;

/**
 * A container object which holds a value and identifies whether a value is present.
 *
 * @param <V> the value type
 */
public final class OptionalValue<V> {
    private static final OptionalValue<?> ABSENT = new OptionalValue<>(false, null);
    private static final OptionalValue<?> PRESENT_NULL = new OptionalValue<>(true, null);

    private final boolean present;
    private final V value;

    /**
     * Creates empty {@link OptionalValue} - {@link #isPresent()} returns {@code false} and {@link #get()} {@code null}.
     *
     * @param <V> the value type
     * @return empty value.
     */
    public static <V> OptionalValue<V> absent() {
        @SuppressWarnings("unchecked")
        OptionalValue<V> absent = (OptionalValue<V>) ABSENT;
        return absent;
    }

    /**
     * Creates present {@link OptionalValue} - {@link #isPresent()} returns {@code true}.
     *
     * @param value value
     * @param <V>   type of the present value.
     * @return present value.
     */
    public static <V> OptionalValue<V> present(V value) {
        @SuppressWarnings("unchecked")
        OptionalValue<V> present = value == null ? (OptionalValue<V>) PRESENT_NULL : new OptionalValue<>(true, value);
        return present;
    }

    private OptionalValue(boolean present, V value) {
        assert value == null || present : "when value != null, present flag must be true";
        this.present = present;
        this.value = value;
    }

    /**
     * Checks whether a value is present.
     *
     * @return {@code true} when a value is present.
     * @see #isAbsent()
     */
    public boolean isPresent() {
        return present;
    }

    /**
     * Checks whether a value is absent.
     *
     * @return {@code true} when the value is not present.
     * @see #isPresent()
     */
    public boolean isAbsent() {
        return !present;
    }

    /**
     * Provides value.
     *
     * @return value.
     * @throws IllegalStateException when a value is not present - {@link #isAbsent()} returns {@code true}.
     */
    public V get() {
        if (!present) {
            throw new IllegalStateException("Value is absent.");
        }
        return value;
    }

    /**
     * Retirns value or {@code null}.
     *
     * @return value or {@code null} when value is not present.
     */
    public V getOrNull() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OptionalValue<?> entry = (OptionalValue<?>) o;
        return present == entry.present && Objects.equals(value, entry.value);
    }

    @Override
    public int hashCode() {
        int result = (present ? 1 : 0);
        result = 31 * result + Objects.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return Objects.toString(value, "(absent)");
    }
}
