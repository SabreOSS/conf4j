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

public class ByteTypeConverterTest {
    private ByteTypeConverter byteTypeConverter;

    @Before
    public void setUp() {
        byteTypeConverter = new ByteTypeConverter();
    }

    @Test
    public void shouldBeApplicableWhenByteType() {
        // given
        Type type = Byte.class;

        // when
        boolean applicable = byteTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotByteType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = byteTypeConverter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenCheckingIfApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> byteTypeConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        Byte b = (byte) 123;

        // when
        String converted = byteTypeConverter.toString(Byte.class, b, emptyMap());

        // then
        assertThat(converted).isEqualTo("123");
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecified() {
        // given
        Byte b = (byte) 123;
        String format = "00000";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String converted = byteTypeConverter.toString(Byte.class, b, attributes);

        // then
        assertThat(converted).isEqualTo("00123");
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = byteTypeConverter.toString(Byte.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndTypeIsNull() {
        // given
        Byte b = (byte) 123;

        // then
        assertThatThrownBy(() -> byteTypeConverter.toString(null, b, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String byteInString = "123";

        // when
        Byte fromConversion = byteTypeConverter.fromString(Byte.class, byteInString, emptyMap());

        // then
        assertThat(fromConversion).isEqualTo((byte) 123);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecified() {
        // given
        String byteInString = "1.23E2";
        String format = "0.##E0";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        Byte fromConversion = byteTypeConverter.fromString(Byte.class, byteInString, attributes);

        // then
        assertThat(fromConversion).isEqualTo((byte) 123);
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        Byte fromConversion = byteTypeConverter.fromString(Byte.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongFormat() {
        // given
        String byteInString = "7b";
        String format = "wrong format";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> byteTypeConverter.fromString(Byte.class, byteInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert to Byte. The value doesn't match specified format:");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndOutOfRange() {
        // given
        String byteInString = "512";
        String format = "#";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> byteTypeConverter.fromString(Byte.class, byteInString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provided value: 512 is out of Byte type range.");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() {
        // given
        String byteInString = "123";

        // then
        assertThatThrownBy(() -> byteTypeConverter.fromString(null, byteInString, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
