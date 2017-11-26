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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class PeriodTypeConverterTest {
    private PeriodConverter converter;

    @BeforeAll
    public void setUp() {
        converter = new PeriodConverter();
    }

    @Test
    public void shouldConvertFromString() {
        // given
        Period period = Period.ofDays(123);

        // when
        Period readPeriod = converter.fromString(Period.class, "P123D", null);

        // then
        assertThat(readPeriod).isEqualTo(period);
    }

    @Test
    public void shouldConvertToString() {
        // given
        Period period = Period.ofDays(123);

        // when
        String asString = converter.toString(Period.class, period, null);

        // then
        assertThat(asString).isEqualTo("P123D");
    }

    @Test
    public void shouldAcceptPeriodType() {
        // given
        Type type = Period.class;

        // when
        boolean isApplicable = converter.isApplicable(type, null);

        // then
        assertThat(isApplicable).isTrue();
    }

    @Test
    public void shouldNotAcceptUnknownType() {
        // when
        boolean isApplicable = converter.isApplicable(mock(Type.class), null);

        // then
        assertThat(isApplicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongValue() {
        // given
        String toConvert = "XXXXX";

        // then
        assertThatThrownBy(() -> converter.fromString(Period.class, toConvert, null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert to a Period: XXXXX");
    }
}
