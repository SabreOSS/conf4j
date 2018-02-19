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

package com.sabre.oss.conf4j.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.*;
import java.util.Map;

import static com.sabre.oss.conf4j.converter.AbstractNumberConverter.FORMAT;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OffsetDateTimeConverterTest {
    private OffsetDateTimeConverter offsetDateTimeConverter;

    @BeforeEach
    public void setUp() {
        offsetDateTimeConverter = new OffsetDateTimeConverter();
    }

    @Test
    public void shouldBeApplicableWhenOffsetDateTimeType() {
        // given
        Type type = OffsetDateTime.class;

        // when
        boolean applicable = offsetDateTimeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotOffsetDateTimeType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = offsetDateTimeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> offsetDateTimeConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        OffsetDateTime toConvert = OffsetDateTime.now(clock);

        // when
        String converted = offsetDateTimeConverter.toString(OffsetDateTime.class, toConvert, emptyMap());

        // then
        assertThat(converted).isEqualTo("1970-01-01T00:00Z");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecified() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        OffsetDateTime toConvert = OffsetDateTime.now(clock);
        String format = "yyyy-MM-dd HH:mm x";
        Map<String, String> attributes = singletonMap(FORMAT, format);

        // when
        String converted = offsetDateTimeConverter.toString(OffsetDateTime.class, toConvert, attributes);

        // then
        assertThat(converted).isEqualTo("1970-01-01 00:00 +00");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = offsetDateTimeConverter.toString(OffsetDateTime.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndWrongFormat() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        OffsetDateTime toConvert = OffsetDateTime.now(clock);
        String format = "invalid format";
        Map<String, String> attributes = singletonMap(FORMAT, format);

        // then
        assertThatThrownBy(() -> offsetDateTimeConverter.toString(OffsetDateTime.class, toConvert, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert OffsetDateTime to String. Invalid format: 'invalid format'");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        OffsetDateTime toConvert = OffsetDateTime.now(clock);

        // then
        assertThatThrownBy(() -> offsetDateTimeConverter.toString(null, toConvert, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String dateInString = "1970-01-01T00:00Z";

        // when
        OffsetDateTime fromConversion = offsetDateTimeConverter.fromString(OffsetDateTime.class, dateInString, emptyMap());

        // then
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        OffsetDateTime expected = OffsetDateTime.now(clock);
        assertThat(fromConversion).isEqualTo(expected);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecified() {
        // given
        String dateInString = "1970 01 01 00:00 +00";
        String format = "yyyy MM dd HH:mm x";
        Map<String, String> attributes = singletonMap(FORMAT, format);

        // when
        OffsetDateTime fromConversion = offsetDateTimeConverter.fromString(OffsetDateTime.class, dateInString, attributes);

        // then
        LocalDateTime localDateTime = LocalDateTime.of(1970, 1, 1, 0, 0);
        ZoneOffset zoneOffset = ZoneOffset.of("+00");
        OffsetDateTime expected = OffsetDateTime.of(localDateTime, zoneOffset);
        assertThat(fromConversion).isEqualTo(expected);
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        OffsetDateTime fromConversion = offsetDateTimeConverter.fromString(OffsetDateTime.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValueString() {
        // given
        String dateInString = "invalid value string";

        // then
        assertThatThrownBy(() -> offsetDateTimeConverter.fromString(OffsetDateTime.class, dateInString, emptyMap()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert to OffsetDateTime: invalid value string. The value doesn't match specified format null.");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongFormat() {
        // given
        String dateInString = "1970 01 01 00:00 +00";
        String format = "invalid format";
        Map<String, String> attributes = singletonMap(FORMAT, format);

        // then
        assertThatThrownBy(() -> offsetDateTimeConverter.fromString(OffsetDateTime.class, dateInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert to OffsetDateTime: 1970 01 01 00:00 +00. Invalid format: 'invalid format'");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
        OffsetDateTime toConvert = OffsetDateTime.now(clock);

        // then
        assertThatThrownBy(() -> offsetDateTimeConverter.toString(null, toConvert, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
