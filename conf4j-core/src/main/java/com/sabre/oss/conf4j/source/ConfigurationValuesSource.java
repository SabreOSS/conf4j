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

import java.util.Collection;
import java.util.Map;

/**
 * Provides read-only abstraction layer over the configuration source(s). Value source is a set
 * of configuration entries {@link ConfigurationEntry} consisting of configuration key and associated value.
 * Such data can be stored for example in the property files, in the database or even may be hardcoded
 * in the code.
 * <p>
 * Configuration value source is usually accessed from multiple threads and it should be <i>thread safe</i>,
 * but it is not a must have requirement. All implementations must explicitly document thread safety.
 * </p>
 *
 * @see WritableConfigurationValuesSource
 */
public interface ConfigurationValuesSource {

    /**
     * Return a value associated with a given key.
     *
     * @param key        configuration key
     * @param attributes custom meta-data associated with property. It can be {@code null}.
     * @return (non null) {@link OptionalValue} which holds configuration value. Use {@link OptionalValue#isPresent()}
     * for checking if the {@code key} is present in the source.
     * @throws NullPointerException when key is {@code null}.
     */
    OptionalValue<String> getValue(String key, Map<String, String> attributes);

    /**
     * Returns first configuration value associated with the {@code keys}.
     *
     * @param keys       collection of configuration keys which will be examined in order. It must be not {@code null}
     *                   nor contains {@code null} elements.
     * @param attributes custom meta-data associated with property. It can be {@code null}.
     * @return configuration entry for the first key on the {@code keys} collection or {@code null}
     * when value is not found.
     * @throws NullPointerException when {@code keys} or any key in {@code keys} is {@code null}.
     */
    default ConfigurationEntry findEntry(Collection<String> keys, Map<String, String> attributes) {
        return ConfigurationValuesSourceUtils.findEntry(this, keys, attributes);
    }
}
