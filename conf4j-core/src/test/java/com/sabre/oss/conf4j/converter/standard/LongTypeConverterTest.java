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
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LongTypeConverterTest {
    private LongTypeConverter longTypeConverter;

    @Before
    public void setUp() {
        longTypeConverter = new LongTypeConverter();
    }

    @Test
    public void shouldBeApplicableWhenLongType() {
        // given
        Type type = Long.class;

        // when
        boolean applicable = longTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotLongType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = longTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> longTypeConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Long aLong = (long) 1234;

        // when
        String converted = longTypeConverter.toString(Long.class, aLong, emptyMap());

        // then
        assertThat(converted).isEqualTo("1234");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecified() {
        // given
        Long aLong = (long) 1234;
        String format = "000000";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = longTypeConverter.toString(Long.class, aLong, attributes);

        // then
        assertThat(converted).isEqualTo("001234");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = longTypeConverter.toString(Long.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Long aLong = (long) 1234;

        // then
        assertThatThrownBy(() -> longTypeConverter.toString(null, aLong, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String longInString = "1234";

        // when
        Long fromConversion = longTypeConverter.fromString(Long.class, longInString, emptyMap());

        // then
        assertThat(fromConversion).isEqualTo((long) 1234);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecified() {
        // given
        String longInString = "1.2345E6";
        String format = "0.####E0";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Long fromConversion = longTypeConverter.fromString(Long.class, longInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo(1234500);
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        Long fromConversion = longTypeConverter.fromString(Long.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValue() {
        // given
        String longInString = "12,350.22";
        String format = "%x";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> longTypeConverter.fromString(Long.class, longInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Long");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        String longInString = "1234";

        // then
        assertThatThrownBy(() -> longTypeConverter.fromString(null, longInString, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
