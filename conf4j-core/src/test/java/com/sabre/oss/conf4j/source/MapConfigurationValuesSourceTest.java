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

import org.junit.Test;

import static com.sabre.oss.conf4j.internal.utils.MapUtils.of;
import static org.assertj.core.api.Assertions.assertThat;

public class MapConfigurationValuesSourceTest {

    @Test
    public void shouldGetSingleValueFromUnderlyingMap() {
        // given
        ConfigurationValuesSource source = new MapConfigurationValuesSource(of("key1", "value1", "key2", "value2"));
        // when
        String value = source.getValue("key2", null).get();
        // then
        assertThat(value).isEqualTo("value2");
    }


    @Test
    public void shouldIterateOver() {
        // given
        MapConfigurationValuesSource source = new MapConfigurationValuesSource(of("key1", "value1", "key2", "value2"));
        // when
        Iterable<ConfigurationEntry> iterable = source.getAllConfigurationEntries();
        // then
        assertThat(iterable).containsExactly(new ConfigurationEntry("key1", "value1"), new ConfigurationEntry("key2", "value2"));
    }
}
