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

package com.sabre.oss.conf4j.converter;

import com.sabre.oss.conf4j.converter.standard.IntegerTypeConverter;
import com.sabre.oss.conf4j.converter.standard.StringTypeConverter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ChainedTypeConverterTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldSupportConvertersFromTheChain() {
        TypeConverter<?> converter = new ChainedTypeConverter(new StringTypeConverter(), new IntegerTypeConverter());

        assertThat(converter.isApplicable(String.class)).isTrue();
        assertThat(converter.isApplicable(Integer.class)).isTrue();
        assertThat(converter.isApplicable(Long.class)).isFalse();
    }

    @Test
    public void shouldDelegateToTheConvertersInTheChain() {
        TypeConverter<Object> converter = new ChainedTypeConverter(new StringTypeConverter(), new IntegerTypeConverter());

        assertThat(converter.fromString(String.class, "string")).isEqualTo("string");
        assertThat(converter.fromString(Integer.class, "10")).isEqualTo(10);

        assertThat(converter.toString(String.class, "string")).isEqualTo("string");
        assertThat(converter.toString(Integer.class, 10)).isEqualTo("10");
    }

    @Test
    public void shouldThrowIAEWhenTypeIsNotSupported() {
        TypeConverter<Object> converter = new ChainedTypeConverter(new StringTypeConverter());

        exception.expect(IllegalArgumentException.class);
        converter.fromString(Long.class, "10");
    }


    @Test
    public void shouldThrowNPEWhenNullConvertersChainIsProvided() {
        // expect
        exception.expect(NullPointerException.class);

        // when
        new ChainedTypeConverter((TypeConverter<?>[]) null);
    }

    @Test
    public void shouldThrowIAEWhenThereIsNullConverterInTheChain() {
        // expect
        exception.expect(IllegalArgumentException.class);

        // when
        new ChainedTypeConverter(Arrays.asList(new StringTypeConverter(), null));
    }
}