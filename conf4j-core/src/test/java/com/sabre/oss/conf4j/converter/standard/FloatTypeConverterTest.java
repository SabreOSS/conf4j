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
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FloatTypeConverterTest {
    private FloatTypeConverter floatTypeConverter;

    @Before
    public void setUp() {
        floatTypeConverter = new FloatTypeConverter();
    }

    @Test
    public void shouldBeApplicableWhenFloatType() {
        // given
        Type type = Float.class;

        // when
        boolean applicable = floatTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotFloatType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = floatTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> floatTypeConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Float fl = (float) 12.345;

        // when
        String converted = floatTypeConverter.toString(Float.class, fl, emptyMap());

        // then
        assertThat(converted).isEqualTo("12.345");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedWithoutLocale() {
        // given
        Float fl = (float) 12.345;
        String format = "#.00";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = floatTypeConverter.toString(Float.class, fl, attributes);

        // then
        assertThat(converted).isEqualTo("12.35");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedWithLocale() {
        // given
        Float fl = (float) 12.345;
        String format = "#.00";
        String locale = "de";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", format);
        attributes.put("locale", locale);

        // when
        String converted = floatTypeConverter.toString(Float.class, fl, attributes);

        // then
        assertThat(converted).isEqualTo("12,35");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = floatTypeConverter.toString(Float.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Float fl = (float) 12.345;

        // then
        assertThatThrownBy(() -> floatTypeConverter.toString(null, fl, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String floatInString = "12.345";

        // when
        Float fromConversion = floatTypeConverter.fromString(Float.class, floatInString, emptyMap());

        // then
        assertThat(fromConversion).isEqualTo((float) 12.345);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecifiedWithDefaultLocale() {
        // given
        String floatInString = "12.35";
        String format = "#.00";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Float fromConversion = floatTypeConverter.fromString(Float.class, floatInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo((float) 12.35);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecifiedWithCustomLocale() {
        // given
        String floatInString = "12,35";
        String format = "#.00";
        String locale = "de";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("format", format);
        attributes.put("locale", locale);

        // when
        Float fromConversion = floatTypeConverter.fromString(Float.class, floatInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo((float) 12.35);
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndOutOfRange() {
        // given
        String floatInString = "10.7976931348623157E308";
        String format = "#.#";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> floatTypeConverter.fromString(Float.class, floatInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provided value: Infinity is out of Float type range.");
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        Float fromConversion = floatTypeConverter.fromString(Float.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValue() {
        // given
        String floatInString = "wrong value";
        String format = "format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> floatTypeConverter.fromString(Float.class, floatInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Float. The value doesn't match specified format:");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        String floatInString = "12.345";

        // then
        assertThatThrownBy(() -> floatTypeConverter.fromString(null, floatInString, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
