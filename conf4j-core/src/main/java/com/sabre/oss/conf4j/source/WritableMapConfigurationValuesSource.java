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

import java.util.Map;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Objects.requireNonNull;

/**
 * Configuration value source backed by {@code Map<String, String>}.
 * <p>
 * It <i>may or <b>may not</b> be thread safe</i> - it depends on the backing map.
 */
public class WritableMapConfigurationValuesSource extends MapConfigurationValuesSource implements WritableConfigurationValuesSource {
    /**
     * Constructs values source.
     *
     * @param source the map that holds configuration keys. It cannot contain {@code null} keys.
     * @throws NullPointerException when {@code source} is null.
     */
    public WritableMapConfigurationValuesSource(Map<String, String> source) {
        super(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(String key, String value, Map<String, String> attributes) {
        requireNonNull(key, "key cannot be null");
        source.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionalValue<String> removeValue(String key, Map<String, String> attributes) {
        requireNonNull(key, "key cannot be null");

        return source.containsKey(key) ? present(source.remove(key)) : absent();
    }
}
