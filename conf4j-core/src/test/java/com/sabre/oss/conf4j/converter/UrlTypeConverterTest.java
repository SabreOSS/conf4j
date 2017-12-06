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
import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

public class UrlTypeConverterTest {

    private UrlConverter converter;

    @Before
    public void setUp() {
        converter = new UrlConverter();
    }

    @Test
    public void shouldBeApplicableWhenUrlType() {
        // given
        Type type = URL.class;

        // when
        boolean applicable = converter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotUrlType() {
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
    public void shouldConvertToString() throws MalformedURLException {
        // given
        String urlString = "http://www.example.com/docs/resource1.html";
        URL toConvert = new URL(urlString);

        // when
        String converted = converter.toString(URL.class, toConvert, emptyMap());

        // then
        assertThat(converted).isEqualTo(urlString);
    }

    @Test
    public void shouldReturnNullWhenConvertingToStringAndValueToConvertIsNull() {
        // when
        String converted = converter.toString(URL.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    public void shouldCovertFromString() throws MalformedURLException {
        // given
        String urlInString = "http://www.example.com/docs/resource1.html";

        // when
        URL fromConversion = converter.fromString(URL.class, urlInString, emptyMap());

        // then
        URL expected = new URL(urlInString);
        assertThat(fromConversion).isEqualTo(expected);
    }

    @Test
    public void shouldThrowExceptionWhenMalformedUrl() {
        // given
        String malformedUrlString = "malformed URL";

        // then
        assertThatThrownBy(() -> converter.fromString(URL.class, malformedUrlString, emptyMap()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to convert to URL: malformed URL");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndTypeIsNull() throws MalformedURLException {
        // given
        String urlInString = "http://www.example.com/docs/resource1.html";

        // then
        assertThatThrownBy(() -> converter.fromString(null, urlInString, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    public void shouldReturnNullWhenConvertingFromStringAndValueToConvertIsNull() {
        // when
        URL fromConversion = converter.fromString(URL.class, null, emptyMap());

        // then
        assertThat(fromConversion).isNull();
    }
}
