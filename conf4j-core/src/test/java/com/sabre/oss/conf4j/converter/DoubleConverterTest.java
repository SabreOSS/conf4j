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
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DoubleConverterTest {
    private DoubleConverter doubleTypeConverter;

    @BeforeEach
    public void setUp() {
        doubleTypeConverter = new DoubleConverter();
    }

    @Test
    public void shouldBeApplicableWhenDoubleType() {
        // given
        Type type = Double.class;

        // when
        boolean applicable = doubleTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotDoubleType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = doubleTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> doubleTypeConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Double d = 12.3456;

        // when
        String converted = doubleTypeConverter.toString(Double.class, d, emptyMap());

        // then
        assertThat(converted).isEqualTo("12.3456");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedWithDefaultLocale() {
        // given
        Double d = 12.3456;
        String format = "#.00";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = doubleTypeConverter.toString(Double.class, d, attributes);

        // then
        assertThat(converted).isEqualTo("12.35");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedWithCustomLocale() {
        // given
        Double d = 12.3456;
        String format = "#.00";
        String locale = "de";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", format);
        attributes.put("locale", locale);

        // when
        String converted = doubleTypeConverter.toString(Double.class, d, attributes);

        // then
        assertThat(converted).isEqualTo("12,35");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = doubleTypeConverter.toString(Double.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Double d = 12.3456;

        // then
        assertThatThrownBy(() -> doubleTypeConverter.toString(null, d, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String doubleInString = "12.3456";

        // when
        Double fromConversion = doubleTypeConverter.fromString(Double.class, doubleInString, emptyMap());

        // then
        assertThat(fromConversion).isEqualTo((Double) 12.3456);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecifiedWithoutLocale() {
        // given
        String doubleInString = "12.35";
        String format = "#.00";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Double fromConversion = doubleTypeConverter.fromString(Double.class, doubleInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo((Double) 12.35);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecifiedWithLocale() {
        // given
        String doubleInString = "12,35";
        String format = "#.00";
        String locale = "de";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", format);
        attributes.put("locale", locale);

        // when
        Double fromConversion = doubleTypeConverter.fromString(Double.class, doubleInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo((Double) 12.35);
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        Double fromConversion = doubleTypeConverter.fromString(Double.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringWithFormatAndWrongValue() {
        // given
        String doubleInString = "wrong value";
        String format = "format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> doubleTypeConverter.fromString(Double.class, doubleInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Double. The value doesn't match specified format:");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValue() {
        // given
        String doubleInString = "12,350.22";

        // then
        assertThatThrownBy(() -> doubleTypeConverter.fromString(Double.class, doubleInString, null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Double:");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        String doubleInString = "12.3456";

        // then
        assertThatThrownBy(() -> doubleTypeConverter.fromString(null, doubleInString, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
