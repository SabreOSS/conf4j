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

package com.sabre.oss.conf4j.internal.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.sabre.oss.conf4j.internal.utils.AttributesUtils.attributes;
import static com.sabre.oss.conf4j.internal.utils.AttributesUtils.mergeAttributes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AttributesUtilsTest {

    @Test
    public void shouldReturnNullWhenPropertiesAreNull() {
        // given
        Map<String, String> properties = null;

        // when
        Map<String, String> attributes = attributes(properties);

        // then
        assertThat(attributes).isNull();
    }

    @Test
    public void shouldConstructFromMap() {
        // given
        Map<String, String> properties = MapUtils.of("key1", "val1", "key2", "val2");

        // when
        Map<String, String> attributes = attributes(properties);

        // then
        assertThat(attributes).isNotNull();
        assertThat(attributes).contains(entry("key1", "val1"), entry("key2", "val2"));
    }

    @Test
    public void shouldBeImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> attributes(MapUtils.of("key", "value")).put("anything", "value")
        );
    }

    @Test
    public void shouldMergeToNullWhenBothAreNull() {
        // when
        Map<String, String> merged = mergeAttributes(null, null);

        // then
        assertThat(merged).isNull();
    }

    @Test
    public void shouldMergeToNonNullWhenOneIsNonNull() {
        // given
        Map<String, String> notNullAttributes = attributes(MapUtils.of("key1", "value1"));

        // when
        Map<String, String> merged = mergeAttributes(notNullAttributes, null);

        // then
        assertThat(merged).isSameAs(notNullAttributes);

        // when
        merged = mergeAttributes(null, notNullAttributes);

        // then
        assertThat(merged).isSameAs(notNullAttributes);
    }

    @Test
    public void shouldMerge() {
        // given
        Map<String, String> parent = MapUtils.of("key1", "value1", "key2", "value2");
        Map<String, String> child = MapUtils.of("key2", "value2", "key3", "value3");

        // when
        Map<String, String> merged = mergeAttributes(attributes(parent), attributes(child));

        // then
        assertThat(merged).containsExactly(entry("key1", "value1"), entry("key2", "value2"), entry("key3", "value3"));
    }

    @Test
    public void shouldProvideSameInstanceForSameProperties() {
        // given
        Map<String, String> properties = MapUtils.of("key1", "value1", "key2", "value2");
        Map<String, String> sameProperties = new HashMap<>(properties);


        // when
        Map<String, String> attributes = attributes(properties);
        Map<String, String> attributesForSameProperties = attributes(sameProperties);

        // then
        assertThat(attributesForSameProperties).isSameAs(attributes);
    }
}
