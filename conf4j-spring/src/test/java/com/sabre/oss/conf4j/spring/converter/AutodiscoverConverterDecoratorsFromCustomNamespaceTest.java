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

import com.sabre.oss.conf4j.converter.DecoratingConverterFactory;
import com.sabre.oss.conf4j.spring.AbstractContextTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.sabre.oss.conf4j.spring.Conf4jSpringConstants.CONF4J_TYPE_CONVERTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.getField;

@ContextConfiguration(classes = AutodiscoverConverterDecoratorsFromCustomNamespaceTest.class)
@ImportResource("classpath:converter/autodiscover-delegating-converter-factories.spring.test.xml")
public class AutodiscoverConverterDecoratorsFromCustomNamespaceTest extends AbstractContextTest {
    @Test
    public void shouldAutodiscoverAllConvertersRegisteredInContext() {
        isRegistered(AggregatedConverter.class, CONF4J_TYPE_CONVERTER);
        AggregatedConverter converter = applicationContext.getBean(AggregatedConverter.class);

        assertThat(converter).isNotNull();
        @SuppressWarnings("unchecked")
        List<DecoratingConverterFactory> autowired =
                (List<DecoratingConverterFactory>) getField(converter, "autowiredFactories");

        assertThat(autowired)
                .hasSize(3)
                .containsExactly(applicationContext.getBean(IntegerDecoratingConverterFactory.class),
                        applicationContext.getBean(LongDecoratingConverterFactory.class),
                        applicationContext.getBean(DefaultDecoratingConverterFactory.class));
    }
}
