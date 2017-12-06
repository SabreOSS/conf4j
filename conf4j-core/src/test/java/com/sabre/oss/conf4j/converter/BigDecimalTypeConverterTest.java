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

import java.math.BigDecimal;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

public class BigDecimalTypeConverterTest {
    private BigDecimalConverter converter;

    @Before
    public void setUp() {
        converter = new BigDecimalConverter();
    }

    @Test
    public void shouldBeApplicableToBigDecimal() {
        // when
        boolean applicable = converter.isApplicable(BigDecimal.class, null);

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableToNonBigDecimal() {
        // when
        boolean applicable = converter.isApplicable(Object.class, null);

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldConvertValuesFromStringWhenFormatNotSpecified() {
        // given
        String stringValue = "100.13";

        // when
        BigDecimal bigDecimal = converter.fromString(BigDecimal.class, stringValue, null);

        // then
        assertThat(bigDecimal).isEqualTo(BigDecimal.valueOf(10013, 2));
    }

    @Test
    public void shouldConvertValuesFromStringWhenFormatSpecified() {
        // given
        String stringValue = "100.130";
        String format = "#.000";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        BigDecimal bigDecimal = converter.fromString(BigDecimal.class, stringValue, attributes);

        // then
        assertThat(bigDecimal).isEqualTo(BigDecimal.valueOf(100130, 3));
    }

    @Test
    public void shouldThrowExceptionWhenReadingIllegalValuesFromString() {
        // given
        String stringValue = "two hundred";

        // when
        try {
            converter.fromString(BigDecimal.class, stringValue, null);
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

            // then
            assertThat(e).hasMessage("Unable to convert to BigDecimal: " + stringValue);
        }
    }

    @Test
    public void shouldWriteValueAsStringWhenFormatNotSpecified() {
        // given
        BigDecimal bigDecimal = BigDecimal.valueOf(123, 1);

        // when
        String asString = converter.toString(BigDecimal.class, bigDecimal, null);

        // then
        assertThat(asString).isEqualTo("12.3");
    }

    @Test
    public void shouldWriteValueAsStringWhenFormatSpecified() {
        // given
        BigDecimal bigDecimal = BigDecimal.valueOf(123, 1);
        String format = "#.00";
        Map<String, String> attributes = singletonMap("format", format);

        // when
        String asString = converter.toString(BigDecimal.class, bigDecimal, attributes);

        // then
        assertThat(asString).isEqualTo("12.30");
    }

    @Test
    public void shouldWriteNullValueAsString() {
        // when
        String asString = converter.toString(BigDecimal.class, null, null);

        // then
        assertThat(asString).isNull();
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        BigDecimal fromConversion = converter.fromString(BigDecimal.class, null, null);

        // then
        assertThat(fromConversion).isNull();
    }

    @Test
    public void shouldThrowWhenReadingIllegalValuesWithFormat() {
        // given
        String bigDecimalAsString = "wrong value";
        String format = "#.00";
        Map<String, String> attributes = singletonMap("format", format);

        // then
        assertThatThrownBy(() -> converter.fromString(BigDecimal.class, bigDecimalAsString, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The value doesn't match specified format");
    }
}
