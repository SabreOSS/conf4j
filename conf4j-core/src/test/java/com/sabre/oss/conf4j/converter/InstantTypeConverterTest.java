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

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InstantTypeConverterTest {
    private InstantConverter instantConverter;

    @Before
    public void setUp() {
        instantConverter = new InstantConverter();
    }

    @Test
    public void shouldBeApplicableWhenInstantType() {
        // given
        Type type = Instant.class;

        // when
        boolean applicable = instantConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotInstantType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = instantConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> instantConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Instant toConvert = Instant.EPOCH;

        // when
        String converted = instantConverter.toString(Instant.class, toConvert, emptyMap());

        // then
        assertThat(converted).isEqualTo("1970-01-01T00:00:00Z");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedAndDefaultZone() {
        // given
        Instant toConvert = Instant.EPOCH;
        String format = "yyyy MM dd HH:mm";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = instantConverter.toString(Instant.class, toConvert, attributes);

        // then
        assertThat(converted).isEqualTo("1969 12 31 18:00");
    }

    @Test
    public void shouldConvertToStringWhenFormatAndZoneSpecified() {
        // given
        Instant toConvert = Instant.EPOCH;
        String format = "yyyy MM dd HH:mm";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", format);
        attributes.put("zone", "Europe/London");

        // when
        String converted = instantConverter.toString(Instant.class, toConvert, attributes);

        // then
        assertThat(converted).isEqualTo("1970 01 01 01:00");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedAndWrongZone() {
        // given
        Instant toConvert = Instant.EPOCH;
        String format = "yyyy MM dd HH:mm";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("zone", "null");
        attributes.put("format", format);

        // when
        String converted = instantConverter.toString(Instant.class, toConvert, attributes);

        // then
        assertThat(converted).isEqualTo("1969 12 31 18:00");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = instantConverter.toString(Instant.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndWrongFormat() {
        // given
        Instant toConvert = Instant.EPOCH;
        String format = "invalid format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> instantConverter.toString(Instant.class, toConvert, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert Instant to String. Invalid format: 'invalid format'");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Instant toConvert = Instant.EPOCH;

        // then
        assertThatThrownBy(() -> instantConverter.toString(null, toConvert, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String dateInString = "1970-01-01T00:00:00Z";

        // when
        Instant fromConversion = instantConverter.fromString(Instant.class, dateInString, emptyMap());

        // then
        Instant expected = Instant.EPOCH;
        assertThat(fromConversion).isEqualTo(expected);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecified() {
        // given
        String dateInString = "19700101 00:00:00 +00";
        String format = "yyyyMMdd HH:mm:ss x";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Instant fromConversion = instantConverter.fromString(Instant.class, dateInString, attributes);

        // then
        Instant expected = Instant.EPOCH;
        assertThat(fromConversion).isEqualTo(expected);
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        Instant fromConversion = instantConverter.fromString(Instant.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValueString() {
        // given
        String dateInString = "invalid value string";

        // then
        assertThatThrownBy(() -> instantConverter.fromString(Instant.class, dateInString, emptyMap()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert to Instant: invalid value string. The value doesn't match specified format null.");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongFormat() {
        // given
        String dateInString = "1970 01 01 00:00 Z";
        String format = "invalid format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> instantConverter.fromString(Instant.class, dateInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert to Instant: 1970 01 01 00:00 Z. Invalid format: 'invalid format'");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        Instant toConvert = Instant.EPOCH;

        // then
        assertThatThrownBy(() -> instantConverter.toString(null, toConvert, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
