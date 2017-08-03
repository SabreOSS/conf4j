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

import static java.util.Objects.requireNonNull;

final class ConfigurationValuesSourceUtils {
    private ConfigurationValuesSourceUtils() {
    }

    static ConfigurationEntry findEntry(
            ConfigurationValuesSource configurationValuesSource,
            Collection<String> keys,
            Map<String, String> attributes
    ) {
        requireNonNull(configurationValuesSource, "configurationValuesSource cannot be null");
        requireNonNull(keys, "keys cannot be null");

        for (String key : keys) {
            OptionalValue<String> value = configurationValuesSource.getValue(key, attributes);
            if (value.isPresent()) {
                return new ConfigurationEntry(key, value.get());
            }
        }
        return null;
    }
}
