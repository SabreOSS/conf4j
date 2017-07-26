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

package com.sabre.oss.conf4j.converter.standard;

import org.junit.Test;

import java.lang.reflect.Type;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DurationTypeConverterTest {
    DurationTypeConverter converter = new DurationTypeConverter();

    @Test
    public void shouldConvertFromString() {
        // given
        Duration duration = Duration.ofMillis(1260535L);
        // when
        Duration readDuration = converter.fromString(Duration.class, "PT21M0.535S");
        // then
        assertThat(readDuration).isEqualTo(duration);
    }

    @Test
    public void shouldConvertToString() {
        // given
        Duration duration = Duration.ofMillis(1260535L);
        // when
        String asString = converter.toString(Duration.class, duration);
        // then
        assertThat(asString).isEqualTo("PT21M0.535S");
    }

    @Test
    public void shouldAcceptReadableDurationType() {
        // given
        Type type = Duration.class;
        // when
        boolean isApplicable = converter.isApplicable(type);
        // then
        assertThat(isApplicable).isTrue();
    }

    @Test
    public void shouldNotAcceptUnknownType() {
        // when
        boolean isApplicable = converter.isApplicable(mock(Type.class));
        // then
        assertThat(isApplicable).isFalse();
    }
}
