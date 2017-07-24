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

import com.sabre.oss.conf4j.internal.utils.MapUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static com.sabre.oss.conf4j.source.Attributes.attributes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class AttributesTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldReturnNullWhenPropertiesAreNull() {
        // given
        Map<String, String> properties = null;

        // when
        Attributes attributes = attributes(properties);

        // then
        assertThat(attributes).isNull();
    }

    @Test
    public void shouldConstructFromMap() {
        // given
        Map<String, String> properties = MapUtils.of("key1", "val1", "key2", "val2");

        // when
        Attributes attributes = attributes(properties);

        // then
        assertThat(attributes).isNotNull();
        assertThat(attributes.getAttributes()).contains(entry("key1", "val1"), entry("key2", "val2"));
    }

    @Test
    public void shouldBeImmutable() {
        // expect
        exception.expect(UnsupportedOperationException.class);

        // when
        attributes(MapUtils.of("key", "value")).getAttributes().put("anything", "value");
    }

    @Test
    public void shouldMergeToNullWhenBothAreNull() {
        // when
        Attributes merged = Attributes.merge(null, null);

        // then
        assertThat(merged).isNull();
    }

    @Test
    public void shouldMergeToNonNullWhenOneIsNonNull() {
        // given
        Attributes notNullAttributes = attributes(MapUtils.of("key1", "value1"));

        // when
        Attributes merged = Attributes.merge(notNullAttributes, null);

        // then
        assertThat(merged).isSameAs(notNullAttributes);

        // when
        merged = Attributes.merge(null, notNullAttributes);

        // then
        assertThat(merged).isSameAs(notNullAttributes);
    }

    @Test
    public void shouldMerge() {
        // given
        Map<String, String> parent = MapUtils.of("key1", "value1", "key2", "value2");
        Map<String, String> child = MapUtils.of("key2", "value2", "key3", "value3");

        // when
        Attributes merged = Attributes.merge(attributes(parent), attributes(child));

        // then
        assertThat(merged.getAttributes()).containsExactly(entry("key1", "value1"), entry("key2", "value2"), entry("key3", "value3"));
    }

    @Test
    public void shouldProvideSameInstanceForSameProperties() {
        // given
        Map<String, String> properties = MapUtils.of("key1", "value1", "key2", "value2");
        Map<String, String> sameProperties = new HashMap<>(properties);


        // when
        Attributes attributes = attributes(properties);
        Attributes attributesForSameProperties = attributes(sameProperties);

        // then
        assertThat(attributesForSameProperties).isSameAs(attributes);
    }
}