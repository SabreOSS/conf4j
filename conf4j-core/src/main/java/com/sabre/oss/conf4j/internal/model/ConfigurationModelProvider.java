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

package com.sabre.oss.conf4j.internal.model;

/**
 * Provides {@link ConfigurationModel} extracted from configuration type.
 * <p>
 * There may be different strategies to obtain a model (for example from annotations).
 */
public interface ConfigurationModelProvider {
    /**
     * Checks that {@code type} is a configuration type.
     *
     * @param type type to check.
     * @return {@code true} when {@code type} is configuration type, {@code false} otherwise.
     * @throws NullPointerException when {@code type} is {@code null}.
     */
    boolean isConfigurationType(Class<?> type);

    /**
     * Provides configuration model for {@code configurationType}.
     *
     * @param configurationType configuration type.
     * @return configuration model.
     * @throws NullPointerException     when {@code configurationType} is {@code null}.
     * @throws IllegalArgumentException when {@code configurationType} is not a configuration type
     *                                  (see {@link #isConfigurationType(Class)}) or its definition is not valid.
     */
    ConfigurationModel getConfigurationModel(Class<?> configurationType);
}
