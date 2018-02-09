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

package com.sabre.oss.conf4j.json.source;

import com.sabre.oss.conf4j.source.OptionalValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonConfigurationSourceTest {
    private JsonConfigurationSource configurationSource;

    @BeforeEach
    void setUp() {
        String jsonContent = "" +
                "{" +
                "\"collection\": [\n" +
                "\"collectionValue\"," +
                "{" +
                "\"nested\": \"nestedCollectionValue\"" +
                "}" +
                "]," +
                "\"flat\": \"flatValue\"," +
                "\"outer\": {" +
                "\"inner\": \"nestedValue\"" +
                "}" +
                "}";
        configurationSource = new JsonConfigurationSource(new ByteArrayInputStream(jsonContent.getBytes()));
    }

    @ParameterizedTest
    @MethodSource("existingProperties")
    void shouldReferToProperProperty(String propertyName, String expectedValue) {
        // when
        OptionalValue<String> propertyValue = configurationSource.getValue(propertyName, emptyMap());

        // then
        assertThat(propertyValue.isPresent()).isTrue();
        assertThat(propertyValue.get()).isEqualTo(expectedValue);
    }

    @Test
    void shouldBeAbsentWhenNoSuchProperty() {
        // given
        String propertyName = "not existing";

        // when
        OptionalValue<String> propertyValue = configurationSource.getValue(propertyName, emptyMap());

        // then
        assertThat(propertyValue.isAbsent()).isTrue();
        assertThatThrownBy(propertyValue::get)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Value is absent.");
    }

    private static Stream<Arguments> existingProperties() {
        return Stream.of(
                Arguments.of("flat", "flatValue"),
                Arguments.of("outer.inner", "nestedValue"),
                Arguments.of("collection[0]", "collectionValue"),
                Arguments.of("collection[1].nested", "nestedCollectionValue")
        );
    }
}
