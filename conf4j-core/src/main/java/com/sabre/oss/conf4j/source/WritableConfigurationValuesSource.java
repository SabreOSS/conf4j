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

package com.sabre.oss.conf4j.source;

/**
 * Configuration value source which supports modifications to the underlying configuration repository.
 * <p>
 * Configuration value source is usually accessed from multiple threads and it should be <i>thread safe</i>,
 * but it is not an absolute requirement. All implementations must explicitly document thread safety.
 * </p>
 */
public interface WritableConfigurationValuesSource extends ConfigurationValuesSource {
    /**
     * Creates or updates configuration value.
     *
     * @param key   configuration key, {@code null} value is not allowed.
     * @param value configuration value.
     * @throws NullPointerException     when {@code key} is {@code null}.
     * @throws IllegalArgumentException when {@code key} does not meet a store format.
     */
    void setValue(String key, String value);

    /**
     * Creates or updates configuration value.
     *
     * @param key        configuration key, {@code null} value is not allowed.
     * @param value      configuration value.
     * @param attributes custom meta-data associated with property. It can be {@code null}.
     * @throws NullPointerException     when {@code key} is {@code null}.
     * @throws IllegalArgumentException when {@code key} does not meet a store format.
     */
    default void setValue(String key, String value, Attributes attributes) {
        this.setValue(key, value);
    }

    /**
     * Removes the configuration value.
     *
     * @param key configuration key, {@code null} value is not allowed.
     * @return removed value, when it is not present {@link OptionalValue#isPresent()} returns {@code false}.
     * @throws NullPointerException     when {@code key} is {@code null}.
     * @throws IllegalArgumentException when {@code key} does not meet a store format.
     */
    OptionalValue<String> removeValue(String key);

    /**
     * Removes the configuration value.
     *
     * @param key        configuration key, {@code null} value is not allowed.
     * @param attributes custom meta-data associated with property. It can be {@code null}.
     * @return removed value, when it wasn't present {@link OptionalValue#isPresent()} returns {@code false}.
     * @throws NullPointerException     when {@code key} is {@code null}.
     * @throws IllegalArgumentException when {@code key} does not meet a store format.
     */
    default OptionalValue<String> removeValue(String key, Attributes attributes) {
        return removeValue(key);
    }
}
