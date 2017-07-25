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

import com.sabre.oss.conf4j.internal.utils.spring.ConcurrentReferenceHashMap;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/**
 * Represents custom meta-data associated with a configuration property.
 * <p>
 * Custom meta-data can be used by classes which implement {@link ConfigurationValuesSource}
 * to resolve configuration key to configuration value using additional rules.
 * For example, a property file name which should be used as source of values can be provided.
 * </p>
 * This class in immutable.
 */
public final class Attributes implements Serializable {
    private static final long serialVersionUID = -7363608399917768393L;
    private static final ConcurrentMap<Map<String, String>, Attributes> cache = new ConcurrentReferenceHashMap<>();

    private final Map<String, String> attributes;

    /**
     * Cache the hash code.
     */
    private int hash; // default to 0

    /**
     * Creates {@code Attributes} from {@code attributes}.
     *
     * @param attributes custom attributes.
     * @throws NullPointerException when {@code attributes} is {@code null}.
     */
    private Attributes(Map<String, String> attributes) {
        requireNonNull(attributes, "attributes cannot be null");
        this.attributes = unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    /**
     * Creates {@code Attributes} from {@code attributes}.
     *
     * @param attributes custom attributes.
     * @return {@code Attributes} or {@code null} when {@code attributes} are null.
     */
    public static Attributes attributes(Map<String, String> attributes) {
        return (attributes == null) ? null : cache.computeIfAbsent(attributes, (a) -> new Attributes(unmodifiableMap(new LinkedHashMap<>(a))));
    }

    public static Attributes merge(Attributes parent, Attributes child) {
        if (parent == null) {
            return child;
        }
        if (child == null) {
            return parent;
        }

        Map<String, String> merged = new LinkedHashMap<>(parent.getAttributes().size() + child.getAttributes().size());
        merged.putAll(parent.getAttributes());
        merged.putAll(child.getAttributes());

        return new Attributes(unmodifiableMap(merged));
    }

    /**
     * Returns custom attributes.
     *
     * @return custom attributes.
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Attributes that = (Attributes) o;

        return attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && !attributes.isEmpty()) {
            h = attributes.hashCode();
            hash = h;
        }
        return h;
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}
