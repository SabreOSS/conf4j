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
package com.sabre.oss.conf4j.processor;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Specialized {@link ConfigurationValueProcessor} responsible for decrypting configuration value.
 * It delegates value decryption to {@link ConfigurationValueDecrypter}.
 *
 * @see ConfigurationValueDecrypter
 * @see ConfigurationValueProcessor
 */
public class ConfigurationValueDecryptingProcessor implements ConfigurationValueProcessor {
    protected ConfigurationValueDecrypter decrypter;

    /**
     * Constructs a class using  {@link NoOpConfigurationValueDecrypter}.
     * Use {@link #setDecrypter(ConfigurationValueDecrypter)} to set proper decrypter latter.
     */
    public ConfigurationValueDecryptingProcessor() {
        this.decrypter = new NoOpConfigurationValueDecrypter();
    }

    /**
     * Constructs a class and set {@code decrypter}.
     *
     * @param decrypter configuration value decrypter.
     * @throws NullPointerException when {@code decrypter} is {@code null}.
     */
    public ConfigurationValueDecryptingProcessor(ConfigurationValueDecrypter decrypter) {
        this.decrypter = requireNonNull(decrypter, "decrypter cannot be null");
    }

    /**
     * Set configuration value decrypter.
     *
     * @param decrypter configuration value decrypter.
     * @throws NullPointerException when {@code decrypter} is {@code null}.
     */
    public void setDecrypter(ConfigurationValueDecrypter decrypter) {
        this.decrypter = requireNonNull(decrypter, "decrypter cannot be null");
    }

    /**
     * Decrypts configuration value when {@link ConfigurationValue#isEncrypted()} returns {@code true}
     * and {@link ConfigurationValue#getEncryptionProvider()} equals {@link ConfigurationValueDecrypter#getName()}.
     * After decryption the configuration value is updated in {@link ConfigurationValue#setDecryptedValue(String)}.
     *
     * @param value value retrieved form the configuration. It may change as a part of processing.
     * @return object provided in {@code value} parameter.
     * @throws IllegalArgumentException when {@code value} is {@code null}.
     * @throws IllegalStateException    when {@link #decrypter} is not set.
     */
    @Override
    public ConfigurationValue process(ConfigurationValue value) {
        requireNonNull(value, "value cannot be null");

        if (value.isEncrypted() && Objects.equals(value.getEncryptionProvider(), decrypter.getName())) {
            String encrypted = value.getValue();
            String decrypted = decrypter.decrypt(encrypted);
            value.setDecryptedValue(decrypted);
        }
        return value;
    }
}
