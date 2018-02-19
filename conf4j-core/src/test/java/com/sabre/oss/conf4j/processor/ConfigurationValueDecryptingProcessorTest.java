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

package com.sabre.oss.conf4j.processor;

import com.sabre.oss.conf4j.annotation.Encrypted;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ConfigurationValueDecryptingProcessorTest {

    private ConfigurationValueDecryptingProcessor processor;
    private ConfigurationValueDecrypter decrypter = mock(ConfigurationValueDecrypter.class);

    @Test
    public void decryptUnencryptedValueAndResetProvider() {
        // given
        String encrypted = "encrypted";
        String decrypted = "decrypted";

        ConfigurationValue value = new ConfigurationValue(null, encrypted, true, Encrypted.DEFAULT, null);
        processor = new ConfigurationValueDecryptingProcessor(decrypter);

        when(decrypter.getName()).thenReturn(Encrypted.DEFAULT);
        when(decrypter.decrypt(encrypted)).thenReturn(decrypted);

        // when
        ConfigurationValue processed = processor.process(value);

        //then
        assertThat(processed).isSameAs(value);
        assertThat(value.isEncrypted()).isFalse();
        assertThat(value.getValue()).isEqualTo(decrypted);
        verify(decrypter, times(1)).decrypt(anyString());
        verify(decrypter, times(1)).getName();
    }

    @Test
    public void shouldNotDecryptUnencryptedValue() {
        // given
        String val = "value";
        ConfigurationValue value = new ConfigurationValue(null, val, true, null, null);
        processor = new ConfigurationValueDecryptingProcessor(decrypter);

        // when
        processor.process(value);

        //then
        assertThat(value.isEncrypted()).isFalse();
        assertThat(value.getValue()).isEqualTo(val);
        verify(decrypter, never()).getName();
        verify(decrypter, never()).decrypt(anyString());
    }

    @Test
    public void shouldNotDecryptEncryptedValueWhenProviderNameDiffers() {
        // given
        String encrypted = "encrypted";
        String decrypted = "decrypted";
        String encryptionProvider = Encrypted.DEFAULT;

        ConfigurationValue value = new ConfigurationValue(null, encrypted, true, encryptionProvider, null);
        processor = new ConfigurationValueDecryptingProcessor(decrypter);

        when(decrypter.getName()).thenReturn("notMatchingProviderName");
        when(decrypter.decrypt(encrypted)).thenReturn(decrypted);

        // when
        ConfigurationValue processed = processor.process(value);

        //then
        assertThat(processed).isSameAs(value);
        assertThat(value.isEncrypted()).isTrue();
        assertThat(value.getEncryptionProvider()).isEqualTo(encryptionProvider);
        verify(decrypter, times(1)).getName();
        verify(decrypter, never()).decrypt(anyString());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionFromNoOpConfigurationValueDecrypter() {
        assertThrows(IllegalArgumentException.class, () -> {
            processor = new ConfigurationValueDecryptingProcessor();
            processor.process(new ConfigurationValue(null, null, true, Encrypted.DEFAULT, null));
        });
    }
}
