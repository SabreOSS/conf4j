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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IntegerConverterTest {
    private IntegerConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new IntegerConverter();
    }

    @Test
    public void shouldBeApplicableWhenIntegerType() {
        // given
        Type type = Integer.class;

        // when
        boolean applicable = converter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotIntegerType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = converter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> converter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Integer i = 1234;

        // when
        String converted = converter.toString(Integer.class, i, emptyMap());

        // then
        assertThat(converted).isEqualTo("1234");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecified() {
        // given
        Integer i = 1234;
        String format = "000000";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = converter.toString(Integer.class, i, attributes);

        // then
        assertThat(converted).isEqualTo("001234");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = converter.toString(Integer.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Integer i = 1234;

        // then
        assertThatThrownBy(() -> converter.toString(null, i, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String integerInString = "1234";

        // when
        Integer fromConversion = converter.fromString(Integer.class, integerInString, emptyMap());

        // then
        assertThat(fromConversion).isEqualTo(1234);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecified() {
        // given
        String integerInString = "1.234E3";
        String format = "0.##E0";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Integer fromConversion = converter.fromString(Integer.class, integerInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo(1234);
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndOutOfRange() {
        // given
        String integerInString = "2147483648";
        String format = "#";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> converter.fromString(Integer.class, integerInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provided value: 2147483648 is out of Integer type range.");
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        Integer fromConversion = converter.fromString(Integer.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValue() {
        // given
        String integerInString = "12,350.22";
        String format = "%x";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> converter.fromString(Integer.class, integerInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Integer");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        String integerInString = "1234";

        // then
        assertThatThrownBy(() -> converter.fromString(null, integerInString, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
