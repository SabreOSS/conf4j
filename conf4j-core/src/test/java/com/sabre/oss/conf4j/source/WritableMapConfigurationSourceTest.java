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

package com.sabre.oss.conf4j.source;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.sabre.oss.conf4j.internal.utils.MapUtils.of;
import static org.assertj.core.api.Assertions.assertThat;

public class WritableMapConfigurationSourceTest {
    @Test
    public void shouldGetValue() {
        // given
        ConfigurationSource mapConfigurationSource = new WritableMapConfigurationSource(of("key", "value"));
        // when
        OptionalValue<String> receivedValue = mapConfigurationSource.getValue("key", null);
        // then
        assertThat(receivedValue.isPresent()).isTrue();
        assertThat(receivedValue.get()).isEqualTo("value");
    }

    @Test
    public void shouldSetAndGetValue() {
        // given
        WritableConfigurationSource mapConfigurationSource = new WritableMapConfigurationSource(new HashMap<>());
        // when
        mapConfigurationSource.setValue("key", "value", null);
        // then
        OptionalValue<String> value = mapConfigurationSource.getValue("key", null);
        assertThat(value.isPresent()).isTrue();
        assertThat(value.get()).isEqualTo("value");
    }

    @Test
    public void shouldRemoveValue() {
        // given
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        WritableConfigurationSource mapConfigurationSource = new WritableMapConfigurationSource(map);

        // when
        mapConfigurationSource.removeValue("key", null);

        // then
        assertThat(mapConfigurationSource.getValue("key", null).isPresent()).isFalse();
    }
}
