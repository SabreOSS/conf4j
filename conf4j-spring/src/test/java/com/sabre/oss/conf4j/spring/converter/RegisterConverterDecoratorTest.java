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

package com.sabre.oss.conf4j.spring.converter;

import com.sabre.oss.conf4j.converter.IntegerConverter;
import com.sabre.oss.conf4j.converter.JsonLikeConverter;
import com.sabre.oss.conf4j.spring.AbstractContextTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = RegisterConverterDecoratorTest.class)
@ImportResource("classpath:converter/register-delegating-converter-factory.spring.test.xml")
public class RegisterConverterDecoratorTest extends AbstractContextTest {
    private static final String CONF4J_DECORATING_CONVERTER_FACTORY_SUFFIX = "$Conf4jDecoratingConverterFactory";

    @Test
    public void shouldRegisterFactoryWhenAvailableAndNoClassSpecified() {
        Class<IntegerDecoratingConverterFactory> expectedFactory = IntegerDecoratingConverterFactory.class;

        isRegistered(expectedFactory, expectedFactory.getName());

        IntegerDecoratingConverterFactory converterFactory = applicationContext.getBean(IntegerDecoratingConverterFactory.class);
        assertThat(converterFactory.create(new IntegerConverter())).isExactlyInstanceOf(IntegerConverter.class);
    }

    @Test
    public void shouldRegisterDefaultDecoratingConverterFactoryWhenNoFactorySpecified() {
        Class<DefaultDecoratingConverterFactory> expectedFactory = DefaultDecoratingConverterFactory.class;

        isRegistered(expectedFactory, JsonLikeConverter.class.getName() + CONF4J_DECORATING_CONVERTER_FACTORY_SUFFIX);

        DefaultDecoratingConverterFactory converterFactory = applicationContext.getBean(expectedFactory);
        assertThat(converterFactory.create(new IntegerConverter())).isExactlyInstanceOf(JsonLikeConverter.class);
    }
}
