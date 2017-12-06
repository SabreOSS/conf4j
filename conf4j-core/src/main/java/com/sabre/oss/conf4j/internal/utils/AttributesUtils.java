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

package com.sabre.oss.conf4j.internal.utils;

import com.sabre.oss.conf4j.internal.utils.spring.ConcurrentReferenceHashMap;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.unmodifiableMap;

/**
 * Utility class used for processing custom meta-data attributes associated with a configuration property.
 * <p>
 * It ensures that attributes maps are cached to avoid storing copies in memory.
 * In most cases number of different attribute sets is small and storing separate copy for each property
 * is wast of space.
 */
public final class AttributesUtils implements Serializable {
    private static final long serialVersionUID = -7363608399917768393L;

    private static final ConcurrentMap<Map<String, String>, Map<String, String>> cache = new ConcurrentReferenceHashMap<>();

    private AttributesUtils() {
    }

    /**
     * Get attributes from cache. The content of map is the same as in {@code attributes} but result is immutable.
     * Use this method whenever there is a need to store attributes in memory for longer time. Result
     * is provided from cache and there is only one instance of attributes with the same content.
     *
     * @param attributes custom attributes.
     * @return immutable attributes map or {@code null} when {@code attributes} is {@code null}.
     */
    public static Map<String, String> attributes(Map<String, String> attributes) {
        return getCached(attributes, true);
    }

    /**
     * Merge attributes from two sources: {@code parent} and {@code child}. Attributes provided in {@code child}
     * have higher priority - if in {@code parent} and {@code child} there is an attribute with the same name,
     * the value from {@code child} is used in resulting map.
     *
     * @param parent parent attributes (can be {@code null}).
     * @param child  child attributes (can be {@code null}).
     * @return merged attributes map which contains all attributes from {@code parent} and {@code child}
     * or {@code null} when both {@code parent} and {@code child} are {@code null}.
     */
    public static Map<String, String> mergeAttributes(Map<String, String> parent, Map<String, String> child) {
        if (parent == null) {
            return getCached(child, true);
        }

        if (child == null) {
            return getCached(parent, true);
        }

        Map<String, String> merged = new LinkedHashMap<>(parent.size() + child.size());
        merged.putAll(parent);
        merged.putAll(child);

        return getCached(unmodifiableMap(merged), false);
    }

    private static Map<String, String> getCached(Map<String, String> attributes, boolean wrapRequired) {
        if (attributes == null) {
            return null;
        }

        Map<String, String> cached = cache.get(attributes);
        if (cached != null) {
            return cached;
        }

        cached = wrapRequired ? unmodifiableMap(new LinkedHashMap<>(attributes)) : attributes;
        Map<String, String> prev = cache.putIfAbsent(cached, cached);

        return prev != null ? prev : cached;
    }
}
