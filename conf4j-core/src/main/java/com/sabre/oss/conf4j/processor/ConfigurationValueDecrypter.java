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

import com.sabre.oss.conf4j.annotation.Encrypted;

/**
 * Interface which is implemented by classes that allows decrypting configuration value.
 *
 * @see ConfigurationValueDecryptingProcessor
 */
public interface ConfigurationValueDecrypter {
    /**
     * Provides the name of the decrypter. Use {@link Encrypted#DEFAULT} to indicated its default decrypter.
     *
     * @return decrypter name, must not be {@code null}.
     * @see Encrypted#value()
     */
    String getName();

    /**
     * Decrypts value.
     *
     * @param value for decrypting.
     * @return decrypted value.
     * @throws IllegalArgumentException when the format of the decrypted value is invalid.
     */
    String decrypt(String value);
}
