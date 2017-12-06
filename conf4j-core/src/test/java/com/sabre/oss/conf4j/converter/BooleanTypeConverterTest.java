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
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BooleanTypeConverterTest {
    private BooleanConverter booleanTypeConverter;

    @Before
    public void setUp() {
        booleanTypeConverter = new BooleanConverter();
    }

    @Test
    public void shouldBeApplicableWhenBooleanType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = booleanTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotBooleanType() {
        // given
        Type type = Integer.class;

        // when
        boolean applicable = booleanTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> booleanTypeConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecifiedAndTrue() {
        // given
        Boolean b = true;

        // when
        String converted = booleanTypeConverter.toString(Boolean.class, b, emptyMap());

        // then
        assertThat(converted).isEqualTo("true");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecifiedAndFalse() {
        // given
        Boolean b = false;

        // when
        String converted = booleanTypeConverter.toString(Boolean.class, b, emptyMap());

        // then
        assertThat(converted).isEqualTo("false");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedAndTrue() {
        // given
        Boolean b = true;
        String format = "yes/no";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = booleanTypeConverter.toString(Boolean.class, b, attributes);

        // then
        assertThat(converted).isEqualTo("yes");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedAndFalse() {
        // given
        Boolean b = false;
        String format = "yes/no";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = booleanTypeConverter.toString(Boolean.class, b, attributes);

        // then
        assertThat(converted).isEqualTo("no");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = booleanTypeConverter.toString(Boolean.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndWrongFormat() {
        // given
        Boolean toConvert = true;
        String format = "wrong format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> booleanTypeConverter.toString(Boolean.class, toConvert, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Invalid 'format' meta-attribute value, it must contain '/' separator character. Provided value is 'wrong format'.");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValue() {
        // given
        String aString = "wrong value";

        // then
        assertThatThrownBy(() -> booleanTypeConverter.fromString(Boolean.class, aString, null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Boolean. Unknown value:");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Boolean b = true;

        // then
        assertThatThrownBy(() -> booleanTypeConverter.toString(null, b, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecifiedAndTrue() {
        // given
        String booleanInString = "true";

        // when
        Boolean fromConversion = booleanTypeConverter.fromString(Boolean.class, booleanInString, emptyMap());

        // then
        assertThat(fromConversion).isEqualTo(true);
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecifiedAndFalse() {
        // given
        String booleanInString = "false";

        // when
        Boolean fromConversion = booleanTypeConverter.fromString(Boolean.class, booleanInString, emptyMap());

        // then
        assertThat(fromConversion).isEqualTo(false);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecifiedAndTrue() {
        // given
        String booleanInString = "on";
        String format = "on/off";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Boolean fromConversion = booleanTypeConverter.fromString(Boolean.class, booleanInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo(true);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecifiedAndFalse() {
        // given
        String booleanInString = "off";
        String format = "on/off";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Boolean fromConversion = booleanTypeConverter.fromString(Boolean.class, booleanInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo(false);
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        Boolean fromConversion = booleanTypeConverter.fromString(Boolean.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongFormat() {
        // given
        String booleanInString = "on";
        String format = "wrong format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> booleanTypeConverter.fromString(Boolean.class, booleanInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Invalid 'format' meta-attribute value, it must contain '/' separator character. Provided value is 'wrong format'.");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringWithFormatAndWrongValue() {
        // given
        String booleanInString = "wrong value";
        String format = "yes/no";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> booleanTypeConverter.fromString(Boolean.class, booleanInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Unable to convert to Boolean, values must be either 'yes' or 'no' but provided value is 'wrong value'.");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        String booleanInString = "on";

        // then
        assertThatThrownBy(() -> booleanTypeConverter.fromString(null, booleanInString, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
