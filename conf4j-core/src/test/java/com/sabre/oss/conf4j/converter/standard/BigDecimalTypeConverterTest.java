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

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class BigDecimalTypeConverterTest {
    BigDecimalTypeConverter bigDecimalTypeAdapter = new BigDecimalTypeConverter();

    @Test
    public void shouldReadValuesFromString() {
        // given
        String stringValue = "100.13";
        // when
        BigDecimal bigDecimal = bigDecimalTypeAdapter.fromString(BigDecimal.class, stringValue);
        // then
        assertThat(bigDecimal).isEqualTo(BigDecimal.valueOf(10013, 2));
    }

    @Test
    public void shouldThrowExceptionWhenReadingIllegalValuesFromString() {
        // given
        String stringValue = "two hundred";
        // when
        try {
            bigDecimalTypeAdapter.fromString(BigDecimal.class, stringValue);
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            // then
            assertThat(e).hasMessage("Unable to convert to a BigDecimal: " + stringValue);
        }
    }

    @Test
    public void shouldWriteValueAsString() {
        // given
        BigDecimal bigDecimal = BigDecimal.valueOf(123, 1);
        // when
        String asString = bigDecimalTypeAdapter.toString(BigDecimal.class, bigDecimal);
        // then
        assertThat(asString).isEqualTo("12.3");
    }

    @Test
    public void shouldWriteNullValueAsString() {
        // given
        BigDecimal bigDecimal = null;
        // when
        String asString = bigDecimalTypeAdapter.toString(BigDecimal.class, bigDecimal);
        // then
        assertThat(asString).isNull();
    }

    @Test
    public void shouldBeApplicableToBigDecimal() {
        // when
        boolean applicable = bigDecimalTypeAdapter.isApplicable(BigDecimal.class);
        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableToNonBigDecimal() {
        // when
        boolean applicable = bigDecimalTypeAdapter.isApplicable(Object.class);
        // then
        assertThat(applicable).isFalse();
    }

}
