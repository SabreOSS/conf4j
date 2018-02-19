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

package com.sabre.oss.conf4j.internal.factory;

import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.utils.KeyGenerator;

import java.util.Map;

/**
 * The interface which specifies how to initialize an empty configuration instance.
 */
public interface ConfigurationInitializer {
    /**
     * Initializes current configuration instance.
     */
    void initializeConfiguration();

    /**
     * Creates and initializes new sub-configuration instance.
     *
     * @param subConfigurationModel sub-configuration model.
     * @param keyGenerator          base key generator.
     * @param fallbackKey           base fallback, global key.
     * @param defaultValues         actual default values.
     * @param attributes            actual custom data
     * @return instantiated and initialized sub-configuration value.
     */
    Object createSubConfiguration(
            ConfigurationModel subConfigurationModel, KeyGenerator keyGenerator,
            String fallbackKey, Map<String, String> defaultValues, Map<String, String> attributes);
}
