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

package com.sabre.oss.conf4j.internal.config;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.factory.KeySetUtils;
import com.sabre.oss.conf4j.internal.utils.KeyGenerator;
import com.sabre.oss.conf4j.processor.ConfigurationValue;
import com.sabre.oss.conf4j.processor.ConfigurationValueProcessor;
import com.sabre.oss.conf4j.source.OptionalValue;
import com.sabre.oss.conf4j.source.TestConfigurationValuesSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.emptyKeyGenerator;
import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.keyGenerator;
import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConfigurationValueProviderTest {
    private final KeyGenerator keyGenerator = keyGenerator("fallback", "fallback.p1", "fallback.p1.p2");
    private final List<String> configurationKeys = asList("key", "alternateKey");
    private KeyGenerator fallbackKeyPrefixGenerator = keyGenerator("fallbackKeyPrefix");
    private String fallbackKey = "fallbackKey";
    private final String defaultValue = "methodDefaultValue";
    private final String notEncrypted = null;
    @Spy
    private TestConfigurationValuesSource source;
    @Mock
    private TypeConverter<String> typeConverter;

    private ConfigurationValueProvider provider = new DefaultConfigurationValueProvider(emptyList());

    @Before
    public void before() {
        reset(source);
        when(source.getValue(anyString())).thenReturn(absent());
        when(typeConverter.fromString(any(), anyString())).thenAnswer(invocation -> invocation.getArguments()[1]);
    }

    private PropertyMetadata metadata(List<String> keySet, String defaultValue, String encryptionProvider) {
        return new PropertyMetadata("anything", String.class, null, keySet, present(defaultValue), encryptionProvider, null);
    }

    private List<String> getKeySet() {
        return KeySetUtils.keySet(keyGenerator, configurationKeys, fallbackKeyPrefixGenerator, fallbackKey);
    }

    @Test
    public void shouldReturnFromFallbackKey() {
        // given exact match
        when(source.getValue("fallback.p1.p2.key")).thenReturn(present("value"));

        // when
        OptionalValue<String> result = provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));

        // then
        assertThat(result).isEqualTo(present("value"));
    }

    @Test
    public void shouldReturnFromFallbackAlternateKey() {
        // given exact match
        when(source.getValue("fallback.p1.p2.alternateKey")).thenReturn(present("value"));

        // when
        OptionalValue<String> result = provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));

        // then
        assertThat(result).isEqualTo(present("value"));
    }

    @Test
    public void shouldReturnFromFallbackPrefix() {
        // given
        when(source.getValue("fallback.key")).thenReturn(present("value"));

        // when
        OptionalValue<String> result = provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));

        // then
        assertThat(result).isEqualTo(present("value"));
    }


    @Test
    public void shouldReturnFromFallbackKeyPrefix() {
        // given
        when(source.getValue("fallbackKeyPrefix.key")).thenReturn(present("value"));

        // when
        OptionalValue<String> result = provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));

        // then
        assertThat(result).isEqualTo(present("value"));
    }

    @Test
    public void shouldNotReturnFallbackKeyPrefixValueWhenFallbackKeyPrefixIsEmpty() {
        // given
        when(source.getValue(".key")).thenReturn(present("value"));
        fallbackKeyPrefixGenerator = emptyKeyGenerator();

        // when
        OptionalValue<String> result = provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));

        // then
        assertThat(result).isEqualTo(present("methodDefaultValue"));
    }

    @Test
    public void shouldReturnFallbackKeyValue() {
        // given
        when(source.getValue("fallbackKey")).thenReturn(present("value"));

        // when
        OptionalValue<String> result = provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));

        // then
        assertThat(result).isEqualTo(present("value"));
    }

    @Test
    public void shouldNotReturnFallbackKeyValueWhenFallbackKeyIsEmpty() {
        // given
        when(source.getValue("")).thenReturn(present("value"));
        fallbackKey = null;

        // when
        OptionalValue<String> result = provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));

        // then
        assertThat(result).isEqualTo(present("methodDefaultValue"));
    }

    @Test
    public void shouldReturnMethodDefaultValue() {
        // when
        OptionalValue<String> result = provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));

        // then
        assertThat(result).isEqualTo(present("methodDefaultValue"));
    }

    @Test
    public void shouldCallValueProcessor() {
        // given
        ConfigurationValueProcessor configurationValueProcessor = mock(ConfigurationValueProcessor.class);
        when(configurationValueProcessor.process(any(ConfigurationValue.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(source.getValue("fallback.key")).thenReturn(present("fallback.key"));

        provider = new DefaultConfigurationValueProvider(singletonList(configurationValueProcessor));
        // when
        provider.getConfigurationValue(typeConverter, source, metadata(getKeySet(), defaultValue, notEncrypted));
        // then
        verify(configurationValueProcessor, times(1)).process(any(ConfigurationValue.class));
    }

}
