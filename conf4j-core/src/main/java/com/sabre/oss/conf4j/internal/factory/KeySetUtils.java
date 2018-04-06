/*
 * MIT License
 *
 * Copyright 2017-2018 Sabre GLBL Inc.
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

package com.sabre.oss.conf4j.internal.factory;

import com.sabre.oss.conf4j.internal.utils.KeyGenerator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class KeySetUtils {
    private KeySetUtils() {
    }

    public static List<String> keySet(KeyGenerator keyGenerator, List<String> keys, KeyGenerator fallbackKeyPrefixGenerator, String fallbackKey) {
        requireNonNull(keyGenerator, "keyGenerator cannot be null");
        requireNonNull(keys, "keys cannot be null");

        Set<String> keySet = new LinkedHashSet<>(keyGenerator.computeKeys(keys));
        if (fallbackKeyPrefixGenerator != null) {
            keySet.addAll(fallbackKeyPrefixGenerator.computeKeys(keys));
        }
        if (isNotBlank(fallbackKey)) {
            keySet.add(fallbackKey);
        }

        List<String> result = new ArrayList<>(keySet.size());
        keySet.forEach(k -> result.add(k.intern()));

        return result;
    }
}
