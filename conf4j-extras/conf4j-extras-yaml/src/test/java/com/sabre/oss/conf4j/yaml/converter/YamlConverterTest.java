/*
 * MIT License
 *
 * Copyright 2017-2018 Sabre GLBL Inc.
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

package com.sabre.oss.conf4j.yaml.converter;

import com.sabre.oss.conf4j.converter.TypeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

import static com.sabre.oss.conf4j.yaml.converter.Yaml.CONVERTER;
import static com.sabre.oss.conf4j.yaml.converter.Yaml.YAML;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YamlConverterTest {
    private TypeConverter<TestClass> typeConverter;

    @BeforeEach
    void setUp() {
        typeConverter = new YamlConverter<>();
    }

    @ParameterizedTest
    @MethodSource("checkApplicable")
    void shouldCheckIfApplicable(Type type, boolean ignoreConverterAttribute, Map<String, String> attributes, boolean expected) {
        // given
        TypeConverter<TestClass> typeConverter = new YamlConverter<>(ignoreConverterAttribute);

        // when
        boolean result = typeConverter.isApplicable(type, attributes);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNullAndIsApplicable() {
        // then
        assertThatThrownBy(() -> typeConverter.isApplicable(null, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    void shouldConvertFromString() {
        // given
        String toConvert = "" +
                "integer: 1\n" +
                "string: test-1\n";

        // when
        TestClass converted = typeConverter.fromString(TestClass.class, toConvert, null);

        // then
        assertThat(converted).isEqualTo(new TestClass(1, "test-1"));
    }

    @Test
    void shouldConvertCollectionFromString() {
        // given
        String toConvert = "" +
                "- integer: 1\n" +
                "  string: test-1\n" +
                "- integer: 2\n" +
                "  string: test-2\n";
        TypeConverter<List<TestClass>> typeConverter = new YamlConverter<>();

        // when
        Collection<TestClass> converted =
                typeConverter.fromString(parameterize(Collection.class, TestClass.class), toConvert, null);

        // then
        assertThat(converted)
                .hasSize(2)
                .containsSequence(
                        new TestClass(1, "test-1"),
                        new TestClass(2, "test-2"));
    }

    @Test
    void shouldConvertMapFromString() {
        // given
        String toConvert = "" +
                "first:\n" +
                "  integer: 1\n" +
                "  string: test-1\n" +
                "second:\n" +
                "  integer: 2\n" +
                "  string: test-2\n";
        TypeConverter<Map<String, TestClass>> typeConverter = new YamlConverter<>();

        // when
        Map<String, TestClass> converted =
                typeConverter.fromString(parameterize(Map.class, String.class, TestClass.class), toConvert, null);

        // then
        assertThat(converted)
                .hasSize(2)
                .containsEntry("first", new TestClass(1, "test-1"))
                .containsEntry("second", new TestClass(2, "test-2"));
    }

    @Test
    void shouldReturnNullWhenFromStringAndValueIsNull() {
        // when
        TestClass converted = typeConverter.fromString(TestClass.class, null, null);

        // then
        assertThat(converted).isNull();
    }

    @Test
    void shouldThrowExceptionWhenImproperYamlFormat() {
        // given
        String toConvert = "" +
                "<integer>1</integer>" +
                "<string>test-1</string>";

        // then
        assertThatThrownBy(() -> typeConverter.fromString(TestClass.class, toConvert, null));
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNullAndFromString() {
        // then
        assertThatThrownBy(() -> typeConverter.fromString(null, null, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    @Test
    void shouldConvertToString() {
        // given
        TestClass toConvert = new TestClass(1, "test-1");

        // when
        String converted = typeConverter.toString(TestClass.class, toConvert, null);

        // then
        assertThat(converted).isEqualTo("" +
                "integer: 1\n" +
                "string: test-1\n");
    }

    @Test
    void shouldConvertCollectionToString() {
        // given
        List<TestClass> toConvert = asList(
                new TestClass(1, "test-1"),
                new TestClass(2, "test-2")
        );
        TypeConverter<List<TestClass>> typeConverter = new YamlConverter<>();

        // when
        String converted =
                typeConverter.toString(parameterize(Collection.class, TestClass.class), toConvert, null);

        // then
        assertThat(converted).isEqualTo("" +
                "- integer: 1\n" +
                "  string: test-1\n" +
                "- integer: 2\n" +
                "  string: test-2\n");
    }

    @Test
    void shouldConvertMapToString() {
        // given
        Map<String, TestClass> toConvert = new HashMap<>();
        toConvert.put("first", new TestClass(1, "test-1"));
        toConvert.put("second", new TestClass(2, "test-2"));
        TypeConverter<Map<String, TestClass>> typeConverter = new YamlConverter<>();

        // when
        String converted =
                typeConverter.toString(parameterize(Map.class, String.class, TestClass.class), toConvert, null);

        // then
        assertThat(converted).isEqualTo("" +
                "first:\n" +
                "  integer: 1\n" +
                "  string: test-1\n" +
                "second:\n" +
                "  integer: 2\n" +
                "  string: test-2\n");
    }

    @Test
    void shouldReturnNullWhenToStringAndValueIsNull() {
        // when
        String converted = typeConverter.toString(TestClass.class, null, null);

        // then
        assertThat(converted).isNull();
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNullAndToString() {
        // then
        assertThatThrownBy(() -> typeConverter.toString(null, null, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null");
    }

    private static Stream<Arguments> checkApplicable() {
        return Stream.of(
                Arguments.of(Integer.class, false, null, false),
                Arguments.of(Integer.class, false, singletonMap(CONVERTER, YAML), true),
                Arguments.of(Integer.class, false, singletonMap(CONVERTER, "jaxB"), false),
                Arguments.of(parameterize(List.class, Integer.class), false, singletonMap(CONVERTER, YAML), true),
                Arguments.of(parameterize(Map.class, String.class, Integer.class), false, singletonMap(CONVERTER, YAML), true),
                Arguments.of(Integer.class, true, null, true),
                Arguments.of(Integer.class, true, singletonMap(CONVERTER, YAML), true),
                Arguments.of(Integer.class, true, singletonMap(CONVERTER, "jaxB"), true),
                Arguments.of(parameterize(List.class, Integer.class), true, singletonMap(CONVERTER, YAML), true),
                Arguments.of(parameterize(Map.class, String.class, Integer.class), true, singletonMap(CONVERTER, YAML), true)
        );
    }

    public static class TestClass {
        private Integer integer;
        private String string;

        TestClass() {
        }

        TestClass(Integer integer, String string) {
            this.integer = integer;
            this.string = string;
        }

        public Integer getInteger() {
            return integer;
        }

        public String getString() {
            return string;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestClass testClass = (TestClass) o;
            return Objects.equals(integer, testClass.integer) &&
                    Objects.equals(string, testClass.string);
        }

        @Override
        public int hashCode() {
            return Objects.hash(integer, string);
        }
    }
}
