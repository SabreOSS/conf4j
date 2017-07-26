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

package com.sabre.oss.conf4j.internal.factory;

import com.sabre.oss.conf4j.internal.utils.KeyGenerator;
import org.junit.Test;

import java.util.List;

import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.emptyKeyGenerator;
import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.keyGenerator;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class KeySetUtilsTest {
    @Test
    public void shouldCreateEmptyKeySet() {
        // given
        KeyGenerator keyGenerator = emptyKeyGenerator();
        List<String> configurationKeys = emptyList();
        KeyGenerator fallbackKeyPrefixGenerator = emptyKeyGenerator();


        // when
        List<String> keys = KeySetUtils.keySet(keyGenerator, configurationKeys, fallbackKeyPrefixGenerator, null);

        // then
        assertThat(keys).isEmpty();
    }

    @Test
    public void shouldGenerateKeySet() {
        // given
        KeyGenerator keyGenerator = keyGenerator("fallback", "fallback.p1", "fallback.p1.p2");
        List<String> configurationKeys = asList("key", "alternateKey", "duplicate", "duplicate");
        KeyGenerator fallbackKeyPrefixGenerator = keyGenerator("fallbackKeyPrefix");
        String fallbackKey = "fallbackKey";

        // when
        List<String> keys = KeySetUtils.keySet(keyGenerator, configurationKeys, fallbackKeyPrefixGenerator, fallbackKey);

        // then
        assertThat(keys).containsSequence(
                "fallback.key",
                "fallback.alternateKey",
                "fallback.duplicate",
                "fallback.p1.key",
                "fallback.p1.alternateKey",
                "fallback.p1.duplicate",
                "fallback.p1.p2.key",
                "fallback.p1.p2.alternateKey",
                "fallback.p1.p2.duplicate",
                "fallbackKeyPrefix.key",
                "fallbackKeyPrefix.alternateKey",
                "fallbackKeyPrefix.duplicate",
                "fallbackKey"
        );
    }

}
