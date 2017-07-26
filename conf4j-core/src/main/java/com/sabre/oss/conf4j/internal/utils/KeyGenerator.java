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

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Utility which simplifies generating configuration keys. A key consists of string blocks separated by a dot.
 * <p>This class is immutable; appending a new block creates a new instance.</p>
 */
public final class KeyGenerator {
    public static final String DELIMITER = ".";
    public static final String LIST_SIZE = "size";
    private static final KeyGenerator EMPTY = new KeyGenerator();

    private final List<String> prefixes;

    public static String computeKey(String prefix, String key) {
        requireNonNull(key, "key cannot be null");
        return isEmpty(prefix) ? key : (prefix + DELIMITER + key);
    }

    public static String computeIndexedKey(String prefix, int index) {
        isTrue(index >= 0, "index must be >= 0", index);
        String indexText = '[' + Integer.toString(index) + ']';
        return isEmpty(prefix) ? indexText : prefix + indexText;
    }

    public static String getSizeKey(String prefix) {
        return computeKey(prefix, LIST_SIZE);
    }

    public static KeyGenerator emptyKeyGenerator() {
        return EMPTY;
    }

    public static KeyGenerator keyGenerator(String... prefixes) {
        return prefixes == null ? emptyKeyGenerator() : new KeyGenerator(asList(prefixes));
    }

    public static KeyGenerator keyGenerator(List<String> prefixes) {
        return prefixes.isEmpty() ? emptyKeyGenerator() : new KeyGenerator(prefixes);
    }

    private KeyGenerator() {
        prefixes = emptyList();
    }

    private KeyGenerator(List<String> prefixes) {
        this.prefixes = prefixes;
    }

    public KeyGenerator append(List<String> suffixes) {
        if (suffixes.isEmpty()) {
            return this;
        }

        if (prefixes.isEmpty()) {
            return new KeyGenerator(suffixes);
        }

        List<String> newPrefixes = new ArrayList<>(prefixes.size() * suffixes.size());

        for (String suffix : suffixes) {
            for (String prefix : prefixes) {
                newPrefixes.add(computeKey(prefix, suffix));
            }
        }
        return new KeyGenerator(newPrefixes);
    }

    public KeyGenerator appendIndex(int index) {
        isTrue(index >= 0, "index must be >= 0", index);
        if (prefixes.isEmpty()) {
            return new KeyGenerator(singletonList(computeIndexedKey(null, index)));
        }
        List<String> newPrefixes = new ArrayList<>(prefixes.size() * 2);
        // add index to the prefix
        for (String prefix : prefixes) {
            newPrefixes.add(computeIndexedKey(prefix, index));
        }
        // as a fallback, adds prefixes - so xxx.yyy will provide a value to any xxx.yyy[*]
        newPrefixes.addAll(prefixes);

        return new KeyGenerator(newPrefixes);
    }

    public List<String> computeKeys(String key) {
        requireNonNull(key, "key cannot be null");

        if (prefixes.isEmpty()) {
            return singletonList(key);
        }
        List<String> result = new ArrayList<>(prefixes.size());
        for (String prefixWithDelimiter : prefixes) {
            result.add(computeKey(prefixWithDelimiter, key));
        }
        return result;
    }

    public List<String> computeKeys(List<String> keys) {
        requireNonNull(keys, "keys cannot be null");

        if (prefixes.isEmpty()) {
            return keys;
        }
        List<String> result = new ArrayList<>(prefixes.size() * keys.size());
        for (String prefixWithDelimiter : prefixes) {
            for (String key : keys) {
                result.add(computeKey(prefixWithDelimiter, key));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return join(prefixes, " ");
    }
}
