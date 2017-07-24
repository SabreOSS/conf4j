/*
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

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumTypeConverterTest {

    enum TestEnum {
        FIRST, SECOND_VALUE
    }

    EnumTypeConverter enumTypeAdapter = new EnumTypeConverter();

    @Test
    public void shouldAcceptEnumType() {
        // given
        Type type = TestEnum.class;
        // when
        boolean isApplicable = enumTypeAdapter.isApplicable(type);
        // then
        assertThat(isApplicable).isTrue();
    }

    @Test
    public void shouldConvertFromString() {
        // given enumTypeAdapter
        // when
        Enum<?> testEnum = enumTypeAdapter.fromString(TestEnum.class, "FIRST");
        // then
        assertThat(testEnum).isEqualTo(TestEnum.FIRST);
    }

    @Test
    public void shouldConvertToString() {
        // given enumTypeAdapter
        // when
        String value = enumTypeAdapter.toString(TestEnum.class, TestEnum.SECOND_VALUE);
        // then
        assertThat(value).isEqualTo(TestEnum.SECOND_VALUE.name());
    }
}
