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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class CharacterTypeConverterTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private CharacterTypeConverter converter = new CharacterTypeConverter();

    @Test
    public void shouldReadValuesFromString() {
        // given
        String stringValue = "A";
        // when
        Character value = converter.fromString(Character.class, stringValue);
        // then
        assertThat(value).isEqualTo('A');
    }

    @Test
    public void shouldThrowExceptionWhenReadingIllegalValuesFromString() {
        // given
        String stringValue = "longer then just on character string";

        // expect
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Unable to convert to a Character: " + stringValue);

        // when
        converter.fromString(Character.class, stringValue);
    }

    @Test
    public void shouldWriteValueAsString() {
        // given
        Character value = 'A';
        // when
        String asString = converter.toString(Character.class, value);
        // then
        assertThat(asString).isEqualTo("A");
    }

    @Test
    public void shouldConvertToNullFromNull() {
        // given
        Character value = null;
        // when
        String asString = converter.toString(Character.class, value);
        // then
        assertThat(asString).isNull();
    }

    @Test
    public void shouldConvertToNullFromEmptyString() {
        // given
        String stringValue = "";
        // when
        Character value = converter.fromString(Character.class, stringValue);
        // then
        assertThat(value).isNull();
    }

    @Test
    public void shouldUnescapeEscapedString() {
        // given
        Character value = '\u0080';
        // when
        String asString = converter.toString(Character.class, value);
        // then
        assertThat(asString).isEqualTo("\\u0080");
    }

    @Test
    public void shouldEscapedString() {
        // given
        String invalidEncodedCharacter = "\\u001";

        // expect
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Unable to convert to a Character: " + invalidEncodedCharacter);

        // when
        converter.fromString(Character.class, invalidEncodedCharacter);
    }

    @Test
    public void shouldBeApplicableToCharacter() {
        // when
        boolean applicable = converter.isApplicable(Character.class);
        // then
        assertThat(applicable).isTrue();
    }

    @Test
    public void shouldNotBeApplicableToNonCharacter() {
        // when
        boolean applicable = converter.isApplicable(Object.class);
        // then
        assertThat(applicable).isFalse();
    }

}
