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

import com.sabre.oss.conf4j.annotation.Default;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.converter.DefaultTypeConverters;
import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.spring.Conf4jSpringConstants;
import com.sabre.oss.conf4j.spring.annotation.ConfigurationType;
import com.sabre.oss.conf4j.spring.annotation.EnableConf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CachingTypeConverterTest {
    private CachingTypeConverter<Long> converter = new CachingTypeConverter<>();

    @Mock
    private TypeConverter<Long> mockConverter;
    @Mock
    private CacheManager mockCacheManager;
    @Mock
    private Cache mockCache;

    @BeforeEach
    public void setUp() {
        Mockito.lenient().when(mockCacheManager.getCache("cacheRegion")).thenReturn(mockCache);
        converter.setTypeConverter(mockConverter);
        converter.setCacheName("cacheRegion");
        converter.setCacheManager(mockCacheManager);
    }

    @Test
    public void shouldDelegateToIsApplicableWithoutCaching() {
        Mockito.lenient().when(mockConverter.isApplicable(Long.class, null)).thenReturn(true);
        when(mockConverter.isApplicable(Object.class, null)).thenReturn(false);

        converter.afterPropertiesSet();

        assertThat(converter.isApplicable(Long.class, null)).isTrue();
        assertThat(converter.isApplicable(Long.class, null)).isTrue();
        assertThat(converter.isApplicable(Object.class, null)).isFalse();

        verify(mockConverter, times(2)).isApplicable(Long.class, null);
        verify(mockConverter, times(1)).isApplicable(Object.class, null);
        verify(mockCache, never()).get(Object.class);
    }

    @Test
    public void shouldDelegateToToStringToWithoutCaching() {

        Mockito.lenient().when(mockConverter.toString(Long.class, 10L, null)).thenReturn("10");
        when(mockConverter.toString(Long.class, 20L, null)).thenReturn("20");

        converter.afterPropertiesSet();

        assertThat(converter.toString(Long.class, 10L, null)).isEqualTo("10");
        assertThat(converter.toString(Long.class, 10L, null)).isEqualTo("10");
        assertThat(converter.toString(Long.class, 20L, null)).isEqualTo("20");

        verify(mockConverter, times(2)).toString(Long.class, 10L, null);
        verify(mockConverter, times(1)).toString(Long.class, 20L, null);
        verify(mockCache, never()).get(Object.class);
    }

    @Test
    public void shouldDelegateToFromStringWithCaching() {
        converter.setCacheManager(new ConcurrentMapCacheManager());


        Mockito.lenient().when(mockConverter.fromString(Long.class, "10", null)).thenReturn(10L);
        when(mockConverter.fromString(Long.class, "20", null)).thenReturn(20L);

        converter.afterPropertiesSet();

        Long val10a = converter.fromString(Long.class, "10", null);
        Long val10b = converter.fromString(Long.class, "10", null);

        assertThat(val10a).isEqualTo(10L);
        assertThat(val10b).isSameAs(val10a);

        assertThat(converter.fromString(Long.class, "20", null)).isEqualTo(20L);

        verify(mockConverter, times(1)).fromString(Long.class, "10", null);
        verify(mockConverter, times(1)).fromString(Long.class, "20", null);
    }

    @Test
    public void shouldIntegrateWithSpring() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfiguration.class)) {
            TypeConverter<?> typeConverter = context.getBean(TypeConverter.class);

            assertThat(typeConverter).isInstanceOf(CachingTypeConverter.class);

            Long val = (Long) typeConverter.fromString(Long.class, "10", null);
            Long val2 = (Long) typeConverter.fromString(Long.class, "10", null);
            assertThat(val).isEqualTo(10L);
            assertThat(val2).isSameAs(val);

            // check the value retrieved from the configuration is the same (same reference) as the value retrieved from the converter
            SampleConfiguration configuration = context.getBean(SampleConfiguration.class);
            Long valueFromConfiguration = configuration.getValue();
            assertThat(valueFromConfiguration).isEqualTo(10L);
            assertThat(valueFromConfiguration).isSameAs(val);

        }
    }

    @Configuration
    @EnableConf4j
    @ConfigurationType(SampleConfiguration.class)
    static class SpringConfiguration {
        @Autowired
        SampleConfiguration sampleConfiguration;

        @Bean(name = Conf4jSpringConstants.CONF4J_TYPE_CONVERTER)
        public TypeConverter<Object> typeConverter() {
            CachingTypeConverter<Object> cachingTypeConverter = new CachingTypeConverter<>();
            cachingTypeConverter.setTypeConverter(DefaultTypeConverters.getDefaultTypeConverter());
            cachingTypeConverter.setCacheManager(new ConcurrentMapCacheManager());
            return cachingTypeConverter;
        }
    }

    public interface SampleConfiguration {
        @Key
        @Default("10")
        Long getValue();
    }
}
