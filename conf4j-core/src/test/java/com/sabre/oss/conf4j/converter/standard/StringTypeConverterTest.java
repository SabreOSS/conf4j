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

import static com.sabre.oss.conf4j.converter.standard.StringConverter.ESCAPE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StringTypeConverterTest {
    private StringConverter converter;

    @Before
    public void setUp() {
        converter = new StringConverter();
    }

    @Test
    public void shouldBeApplicableWhenStringType() {
        // given
        Type type = String.class;

        // when
        boolean applicable = converter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableWhenNotStringType() {
        // given
        Type type = Boolean.class;

        // when
        boolean applicable = converter.isApplicable(type, emptyMap());

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void shouldConvertFromStringWhenFormatNotSpecified() {
        // given
        String in = "One\\\\Two";
        String expected = "One\\Two";

        // when
        String out = converter.fromString(String.class, in, null);

        // then
        assertThat(out).isEqualTo(expected);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecifiedAndFalse() {
        // given
        String in = "One\\\\Two";
        String escape = "false";
        Map<String, String> attributes = singletonMap("escape", escape);

        // when
        String out = converter.fromString(String.class, in, attributes);

        // then
        assertThat(out).isEqualTo(in);
    }

    @Test
    public void shouldConvertFromStringWhenFormatSpecifiedAndTrue() {
        // given
        String in = "One\\\\Two";
        String escape = "true";
        Map<String, String> attributes = singletonMap("escape", escape);

        // when
        String out = converter.fromString(String.class, in, attributes);

        // then
        assertThat(out).isEqualTo("One\\Two");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingFromStringAndWrongEscapeValue() {
        // given
        String in = "One\\Two";
        String escape = "wrong value";
        Map<String, String> attributes = singletonMap("escape", escape);

        // then
        assertThatThrownBy(() -> converter.fromString(String.class, in, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Invalid 'escape' meta-attribute value, it must be either 'true' or 'false', but 'wrong value' is provided.");
    }

    @Test
    public void shouldConvertToStringWhenFormatNotSpecified() {
        // given
        String in = "One\\Two";
        String expected = "One\\\\Two";

        // when
        String out = converter.toString(String.class, in, null);

        // then
        assertThat(out).isEqualTo(expected);
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedAndFalse() {
        // given
        String in = "One\\Two";
        String escape = "false";
        Map<String, String> attributes = singletonMap(ESCAPE, escape);

        // when
        String out = converter.toString(String.class, in, attributes);

        // then
        assertThat(out).isEqualTo(in);
    }

    @Test
    public void shouldConvertToStringWhenFormatSpecifiedAndTrue() {
        // given
        String in = "One\\Two";
        String escape = "true";
        Map<String, String> attributes = singletonMap(ESCAPE, escape);

        // when
        String out = converter.toString(String.class, in, attributes);

        // then
        assertThat(out).isEqualTo("One\\\\Two");
    }

    @Test
    public void shouldThrowExceptionWhenConvertingToStringAndWrongEscapeValue() {
        // given
        String in = "One\\Two";
        String escape = "wrong value";
        Map<String, String> attributes = singletonMap(ESCAPE, escape);

        // then
        assertThatThrownBy(() -> converter.toString(String.class, in, attributes))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Invalid 'escape' meta-attribute value, it must be either 'true' or 'false', but 'wrong value' is provided.");
    }
}
