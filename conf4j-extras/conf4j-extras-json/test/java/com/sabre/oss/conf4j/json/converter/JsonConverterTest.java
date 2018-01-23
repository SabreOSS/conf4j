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

package com.sabre.oss.conf4j.json.converter;

import com.sabre.oss.conf4j.converter.TypeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static com.sabre.oss.conf4j.json.converter.Json.CONVERTER;
import static com.sabre.oss.conf4j.json.converter.Json.JSON;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonConverterTest {
    private TypeConverter<TestClass> typeConverter;

    @BeforeEach
    void setUp() {
        typeConverter = new JsonConverter<>();
    }

    @ParameterizedTest
    @MethodSource("checkApplicable")
    void shouldCheckIfApplicable(boolean ignoreConverterAttribute, Map<String, String> attributes, boolean expected) {
        // given
        TypeConverter<TestClass> typeConverter = new JsonConverter<>(ignoreConverterAttribute);

        // when
        boolean result = typeConverter.isApplicable(Integer.class, attributes);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNullAndIsApplicable() {
        // then
        assertThatThrownBy(() -> typeConverter.isApplicable(null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    void shouldConvertFromString() {
        // given
        String toConvert = "" +
                "{" +
                "   \"integer\": \"10\"," +
                "   \"string\": \"test\"" +
                "}";

        // when
        TestClass converted = typeConverter.fromString(TestClass.class, toConvert, emptyMap());

        // then
        assertThat(converted).isNotNull();
        assertThat(converted.integer).isEqualTo(10);
        assertThat(converted.string).isEqualTo("test");
    }

    @Test
    void shouldThrowAssertionErrorWhenTryToConvertFromStringWithInvalidProperty() {
        // given
        String toConvert = "" +
                "{" +
                "   \"int\": \"10\"," +
                "   \"string\": \"test\"" +
                "}";

        // then
        assertThatThrownBy(() -> typeConverter.fromString(TestClass.class, toConvert, emptyMap()))
                .isExactlyInstanceOf(AssertionError.class)
                .hasMessage("An IOException occurred when this was assumed to be impossible.");
    }

    @Test
    void shouldThrowAssertionErrorWhenTryToConvertFromInvalidJsonString() {
        // given
        String toConvert = "" +
                "   \"integer\": \"10\"," +
                "   \"string\": \"test\"" +
                "}";

        // then
        assertThatThrownBy(() -> typeConverter.fromString(TestClass.class, toConvert, emptyMap()))
                .isExactlyInstanceOf(AssertionError.class)
                .hasMessage("An IOException occurred when this was assumed to be impossible.");
    }

    @Test
    void shouldReturnNullWhenFromStringAndValueIsNull() {
        // when
        TestClass converted = typeConverter.fromString(TestClass.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNullAndFromString() {
        // then
        assertThatThrownBy(() -> typeConverter.fromString(null, null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    void shouldConvertToString() {
        // given
        TestClass toConvert = new TestClass();
        toConvert.integer = 10;
        toConvert.string = "test";

        // when
        String converted = typeConverter.toString(TestClass.class, toConvert, emptyMap());

        // then
        assertThat(converted).isEqualTo("" +
                "{" +
                "\"integer\":10," +
                "\"string\":\"test\"" +
                "}");
    }

    @Test
    void shouldReturnNullWhenToStringAndValueIsNull() {
        // when
        String converted = typeConverter.toString(TestClass.class, null, emptyMap());

        // then
        assertThat(converted).isNull();
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNullAndToString() {
        // then
        assertThatThrownBy(() -> typeConverter.toString(null, null, emptyMap()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    private static Stream<Arguments> checkApplicable() {
        return Stream.of(
                Arguments.of(false, emptyMap(), false),
                Arguments.of(false, singletonMap(CONVERTER, JSON), true),
                Arguments.of(false, singletonMap(CONVERTER, "jaxB"), false),
                Arguments.of(true, emptyMap(), true),
                Arguments.of(true, singletonMap(CONVERTER, JSON), true),
                Arguments.of(true, singletonMap(CONVERTER, "jaxB"), true)
        );
    }

    public static class TestClass {
        private Integer integer;
        private String string;

        public Integer getInteger() {
            return integer;
        }

        public String getString() {
            return string;
        }

        public void setInteger(Integer integer) {
            this.integer = integer;
        }

        public void setString(String string) {
            this.string = string;
        }
    }
}