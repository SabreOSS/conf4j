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

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocalDateTimeTypeConverterTest {

    private LocalDateTimeTypeConverter localDateTimeTypeConverter;

    @Before
    public void setUp() {
        localDateTimeTypeConverter = new LocalDateTimeTypeConverter();
    }

    @Test
    public void shouldBeApplicableWhenLocalDateTimeType() {
        // given
        Type type = LocalDateTime.class;

        // when
        boolean applicable = localDateTimeTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotLocalDateTimeType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = localDateTimeTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> localDateTimeTypeConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        LocalDateTime toConvert = LocalDateTime.now(clock);

        // when
        String converted = localDateTimeTypeConverter.toString(LocalDateTime.class, toConvert, emptyMap());

        // then
        assertThat(converted).isEqualTo("1970-01-01T00:00");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecified() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        LocalDateTime toConvert = LocalDateTime.now(clock);
        String format = "yyyy MM dd HH:mm";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = localDateTimeTypeConverter.toString(LocalDateTime.class, toConvert, attributes);

        // then
        assertThat(converted).isEqualTo("1970 01 01 00:00");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = localDateTimeTypeConverter.toString(LocalDateTime.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndWrongFormat() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        LocalDateTime toConvert = LocalDateTime.now(clock);
        String format = "invalid format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> localDateTimeTypeConverter.toString(LocalDateTime.class, toConvert, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert LocalDateTime to String. Invalid format: 'invalid format'");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        LocalDateTime toConvert = LocalDateTime.now(clock);

        // then
        assertThatThrownBy(() -> localDateTimeTypeConverter.toString(null, toConvert, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String dateInString = "1970-01-01T00:00";

        // when
        LocalDateTime fromConversion = localDateTimeTypeConverter.fromString(LocalDateTime.class, dateInString, emptyMap());

        // then
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        LocalDateTime expected = LocalDateTime.now(clock);
        assertThat(fromConversion).isEqualTo(expected);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecified() {
        // given
        String dateInString = "1970 01 01 00:00";
        String format = "yyyy MM dd HH:mm";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        LocalDateTime fromConversion = localDateTimeTypeConverter.fromString(LocalDateTime.class, dateInString, attributes);

        // then
        LocalDateTime expected = LocalDateTime.of(1970, 1, 1, 0, 0);
        assertThat(fromConversion).isEqualTo(expected);
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        LocalDateTime fromConversion = localDateTimeTypeConverter.fromString(LocalDateTime.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValueString() {
        // given
        String dateInString = "invalid value string";

        // then
        assertThatThrownBy(() -> localDateTimeTypeConverter.fromString(LocalDateTime.class, dateInString, emptyMap()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert to LocalDateTime: invalid value string. The value doesn't match specified format null.");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongFormat() {
        // given
        String dateInString = "1970 01 01 00:00";
        String format = "invalid format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> localDateTimeTypeConverter.fromString(LocalDateTime.class, dateInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert to LocalDateTime: 1970 01 01 00:00. Invalid format: 'invalid format'");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        LocalDateTime toConvert = LocalDateTime.now(clock);

        // then
        assertThatThrownBy(() -> localDateTimeTypeConverter.toString(null, toConvert, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
