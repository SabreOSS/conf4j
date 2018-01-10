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

package com.sabre.oss.conf4j.spring.converter;

import com.sabre.oss.conf4j.converter.DecoratingConverterFactory;
import com.sabre.oss.conf4j.spring.AbstractContextTest;
import com.sabre.oss.conf4j.spring.annotation.EnableConf4j;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.sabre.oss.conf4j.spring.Conf4jSpringConstants.CONF4J_TYPE_CONVERTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.getField;

@ContextConfiguration(classes = AutodiscoverConverterDecoratorsAnnotationsBasedTest.class)
@EnableConf4j
public class AutodiscoverConverterDecoratorsAnnotationsBasedTest extends AbstractContextTest {
    @Test
    public void shouldAutodiscoverAllConvertersRegisteredInContext() {
        isRegistered(AggregatedConverter.class, CONF4J_TYPE_CONVERTER);
        AggregatedConverter converter = applicationContext.getBean(AggregatedConverter.class);

        assertThat(converter).isNotNull();
        List<DecoratingConverterFactory> autowired =
                (List<DecoratingConverterFactory>) getField(converter, "autowiredFactories");

        assertThat(autowired)
                .hasSize(3)
                .containsSequence(applicationContext.getBean(IntegerDecoratingConverterFactory.class,
                        LongDecoratingConverterFactory.class,
                        JsonLikeDecoratingConverterFactory.class));
    }

    @Bean
    @Order(2)
    public DecoratingConverterFactory longDecoratingConverterFactory() {
        return new LongDecoratingConverterFactory();
    }

    @Bean
    @Order(3)
    public DecoratingConverterFactory jsonLikeDecoratingConverterFactory() {
        return new JsonLikeDecoratingConverterFactory();
    }

    @Bean
    @Order(1)
    public DecoratingConverterFactory integerDecoratingConverterFactory() {
        return new IntegerDecoratingConverterFactory();
    }
}
