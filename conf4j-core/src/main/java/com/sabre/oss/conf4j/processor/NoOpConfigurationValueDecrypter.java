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

import static java.util.Objects.requireNonNull;

/**
 * This decrypter always throws {@link IllegalArgumentException} and should be used when decryption is not supported.
 */
public class NoOpConfigurationValueDecrypter implements ConfigurationValueDecrypter {
    private final String name;

    /**
     * Constructs decrypter with name set to {@link Encrypted#DEFAULT}.
     */
    public NoOpConfigurationValueDecrypter() {
        this(Encrypted.DEFAULT);
    }

    /**
     * Constructs with the name specfied by {@code name} parameter.
     *
     * @param name decrypter name, cannot be {@code null}.
     */
    public NoOpConfigurationValueDecrypter(String name) {
        requireNonNull(name, "name cannot be null");
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Always throws {@link IllegalArgumentException}
     *
     * @param value this parameter is ignored.
     * @return never returns any value.
     * @throws IllegalArgumentException always when this method is invoked.
     */
    @Override
    public String decrypt(String value) {
        throw new IllegalArgumentException("Configuration value decryption is not supported.");
    }
}
