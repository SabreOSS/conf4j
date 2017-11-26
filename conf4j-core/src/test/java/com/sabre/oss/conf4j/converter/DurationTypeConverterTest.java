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

package com.sabre.oss.conf4j.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class DurationTypeConverterTest {
    private DurationConverter converter;

    @BeforeAll
    public void setUp() {
        converter = new DurationConverter();
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        Duration duration = Duration.ofMillis(1260535L);

        // when
        Duration readDuration = converter.fromString(Duration.class, "PT21M0.535S", null);

        // then
        assertThat(readDuration).isEqualTo(duration);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecified() {
        // given
        Duration duration = Duration.ofMillis(600000L);
        String format = "HH:mm:ss";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Duration readDuration = converter.fromString(Duration.class, "00:10:00", attributes);

        // then
        assertThat(readDuration).isEqualTo(duration);
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Duration duration = Duration.ofMillis(1260535L);

        // when
        String asString = converter.toString(Duration.class, duration, null);

        // then
        assertThat(asString).isEqualTo("PT21M0.535S");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecified() {
        // given
        Duration duration = Duration.ofHours(1).plusMinutes(10).plusSeconds(20);
        String format = "HH:mm:ss";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String asString = converter.toString(Duration.class, duration, attributes);

        // then
        assertThat(asString).isEqualTo("01:10:20");
    }

    @Test
    public void shouldAcceptReadableDurationType() {
        // given
        Type type = Duration.class;

        // when
        boolean isApplicable = converter.isApplicable(type, null);

        // then
        assertThat(isApplicable).isTrue();
    }

    @Test
    public void shouldNotAcceptUnknownType() {
        // when
        boolean isApplicable = converter.isApplicable(mock(Type.class), null);

        // then
        assertThat(isApplicable).isFalse();
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = converter.toString(Duration.class, null, null);

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValueString() {
        // given
        String durationInString = "invalid value string";

        // then
        assertThatThrownBy(() -> converter.fromString(Duration.class, durationInString, null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Duration");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringWithFormatAndWrongValueString() {
        // given
        String durationInString = "invalid value string";
        String format = "H:i:s";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> converter.fromString(Duration.class, durationInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Duration");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringWithFormatAndWrongValue() {
        // given
        String durationInString = "a wrong duration";
        String format = "HH:mm:ss";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> converter.fromString(Duration.class, durationInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Duration");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringWithFormatAndWrongValue() {
        // given
        Duration duration = Duration.ofMillis(-600000L);
        String format = "HH:mm:ss";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> converter.toString(Duration.class, duration, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert Duration to String. Invalid duration value:");
    }
}
