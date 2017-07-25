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

import static java.util.Objects.requireNonNull;

/**
 * Represents immutable configuration entry which is pair of strings: <i>configuration key</i> and
 * <i>configuration value</i>.
 */
public final class ConfigurationEntry {
    private final String key;
    private final String value;

    /**
     * Constructs configuration entry.
     *
     * @param key   key, cannot be {@code null}.
     * @param value value.
     * @throws NullPointerException when {@code key} is {@code null}.
     */
    public ConfigurationEntry(String key, String value) {
        this.key = requireNonNull(key, "key cannot be null");
        this.value = value;
    }

    /**
     * Configuration key.
     *
     * @return configuration key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Configuration value.
     *
     * @return configuration value.
     */
    public String getValue() {
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

        ConfigurationEntry configurationEntry = (ConfigurationEntry) o;

        return key.equals(configurationEntry.key) &&
                Objects.equals(value, configurationEntry.value);

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + Objects.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return key + '=' + (value == null ? "" : value);
    }
}
