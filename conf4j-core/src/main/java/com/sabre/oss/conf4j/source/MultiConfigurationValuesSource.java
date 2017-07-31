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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static java.util.Objects.requireNonNull;

/**
 * Configuration value source which delegates configuration key lookup to list of {@link ConfigurationValuesSource}.
 * <p>
 * The order of lookup is defined by the list. If the same key is available from multiple sources, the value
 * will be retrieved from the first one on the list.
 * </p>
 * It <i>may or <b>may not</b> be thread safe</i> - it depends on the backing configuration value sources.
 */
public class MultiConfigurationValuesSource implements ConfigurationValuesSource {
    protected final List<ConfigurationValuesSource> sources;

    public MultiConfigurationValuesSource(List<ConfigurationValuesSource> sources) {
        requireNonNull(sources, "sources cannot be null");
        this.sources = new ArrayList<>(sources);
    }

    @Override
    public OptionalValue<String> getValue(String key, Map<String, String> attributes) {
        requireNonNull(key, "key cannot be null");

        for (ConfigurationValuesSource source : sources) {
            OptionalValue<String> value = source.getValue(key, attributes);
            if (value.isPresent()) {
                return value;
            }
        }

        return absent();
    }

    @Override
    public ConfigurationEntry findEntry(Collection<String> keys, Map<String, String> attributes) {
        requireNonNull(keys, "keys cannot be null");

        for (ConfigurationValuesSource source : sources) {
            ConfigurationEntry entry = source.findEntry(keys, attributes);
            if (entry != null) {
                return entry;
            }
        }

        return null;
    }
}
