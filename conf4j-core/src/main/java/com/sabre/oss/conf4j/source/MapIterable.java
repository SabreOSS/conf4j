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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;

/**
 * Utility class making Map&lt;String, String &gt; iterable by lazily transforming {@link Entry}s
 * into {@link ConfigurationEntry}s.
 */
public class MapIterable implements Iterable<ConfigurationEntry> {
    private final Map<String, String> map;

    public MapIterable(Map<String, String> map) {
        requireNonNull(map, "map cannot be null");

        this.map = map;
    }

    @Override
    public Iterator<ConfigurationEntry> iterator() {
        Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
        return new Iterator<ConfigurationEntry>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ConfigurationEntry next() {
                Entry<String, String> next = iterator.next();
                return new ConfigurationEntry(next.getKey(), next.getValue());
            }
        };
    }
}
