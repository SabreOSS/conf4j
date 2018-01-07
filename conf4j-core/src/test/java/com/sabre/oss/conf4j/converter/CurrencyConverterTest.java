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
import java.util.Currency;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CurrencyConverterTest {

    private CurrencyConverter converter;

    @Before
    public void setUp() {
        converter = new CurrencyConverter();
    }

    @Test
    public void shouldBeApplicableWhenCurrencyType() {
        // given
        Type type = Currency.class;

        // when
        boolean applicable = converter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotCurrencyType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = converter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenIsApplicableAndTypeIsNull() {
        // then
        assertThatThrownBy(() -> converter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertToStringWhenLocaleNotSpecified() {
        // given
        Currency toConvert = Currency.getInstance("USD");

        // when
        String converted = converter.toString(Currency.class, toConvert, emptyMap());

        // then
        assertThat(converted).isEqualTo("US Dollar");
    }

    @Test
    public void shouldConvertToStringWhenLocaleSpecified() {
        // given
        Currency toConvert = Currency.getInstance("USD");
        Map<String, String> attributes = singletonMap("locale", "DE");

        // when
        String converted = converter.toString(Currency.class, toConvert, attributes);

        // then
        assertThat(converted).isEqualTo("US-Dollar");
    }

    @Test
    public void shouldConvertToStringWithoutLocaleWhenInvalidLocaleSpecified() {
        // given
        Currency toConvert = Currency.getInstance("USD");
        Map<String, String> attributes = singletonMap("locale", "invalid");

        // when
        String converted = converter.toString(Currency.class, toConvert, attributes);

        // then
        assertThat(converted).isEqualTo("US Dollar");
    }

    @Test
    public void shouldReturnNullWhenToStringAndValueIsNull() {
        // when
        String converted = converter.toString(Currency.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenToStringAndTypeIsNull() {
        // given
        Currency toConvert = Currency.getInstance("USD");

        // then
        assertThatThrownBy(() -> converter.toString(null, toConvert, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldConvertFromString() {
        // given
        String toConvert = "USD";

        // when
        Currency converted = converter.fromString(Currency.class, toConvert, emptyMap());

        // then
        assertThat(converted).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    public void shouldThrowExceptionWhenInvalidFormat() {
        // given
        String toConvert = "invalid";

        // then
        assertThatThrownBy(() -> converter.fromString(Currency.class, toConvert, emptyMap()))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldReturnNullWhenFromStringAndValueIsNull() {
        // when
        Currency converted = converter.fromString(Currency.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenFromStringAndTypeIsNull() {
        // given
        String toConvert = "USD";

        // then
        assertThatThrownBy(() -> converter.fromString(null, toConvert, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }
}
