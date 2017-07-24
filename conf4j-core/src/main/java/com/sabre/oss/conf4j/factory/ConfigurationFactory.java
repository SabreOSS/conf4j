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
package com.sabre.oss.conf4j.factory;

import com.sabre.oss.conf4j.source.ConfigurationValuesSource;

/**
 * This class creates a configuration instance and binds it to the value source.
 * <p>
 * Configuration factory implementation must be thread-safe.
 * </p>
 */
public interface ConfigurationFactory {
    /**
     * Creates configuration class and binds it to {@code valuesSource}. Classes are loaded using
     * class provided by {@code Thread.currentThread().getContextClassLoader()}.
     *
     * @param configurationType configuration class.
     * @param valuesSource      values source.
     * @param <T>               configuration type.
     * @return configuration instance
     */
    <T> T createConfiguration(Class<T> configurationType, ConfigurationValuesSource valuesSource);

    /**
     * Creates configuration class and binds it to {@code valuesSource}.
     *
     * @param configurationType configuration class.
     * @param valuesSource      values source.
     * @param classLoader       class loader, when {@code null} {@code Thread.currentThread().getContextClassLoader()} is used.
     * @param <T>               configuration type.
     * @return configuration instance
     */
    <T> T createConfiguration(Class<T> configurationType, ConfigurationValuesSource valuesSource, ClassLoader classLoader);
}
