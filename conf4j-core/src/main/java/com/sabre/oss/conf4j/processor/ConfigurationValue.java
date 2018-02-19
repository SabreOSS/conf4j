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

import com.sabre.oss.conf4j.annotation.Default;

import java.util.Map;

/**
 * This class represents configuration value retrieved from the configuration value source.
 * It is mutable and may be change by {@link ConfigurationValueProcessor}.
 *
 * @see ConfigurationValueProcessor
 */
public class ConfigurationValue {
    private String configurationKey;
    private String value;
    private boolean defaultValue;
    private String encryptionProvider;
    private Map<String, String> attributes;

    /**
     * Constructs configuration value.
     *
     * @param configurationKey   configuration key the value was retrieved. It may be {@code null} when the key
     *                           is unavailable (for example when the value is fetched from {@link Default} annotation.
     * @param value              configuration value.
     * @param defaultValue       indicates the value is default value assigned with configuration key.
     * @param encryptionProvider encryption provider name, or {@code null} when value is not encrypted.
     * @param attributes         custom meta-data associated with property or {@code null}.
     */
    public ConfigurationValue(String configurationKey, String value, boolean defaultValue, String encryptionProvider, Map<String, String> attributes) {
        this.configurationKey = configurationKey;
        this.value = value;
        this.defaultValue = defaultValue;
        this.encryptionProvider = encryptionProvider;
        this.attributes = attributes;
    }

    /**
     * Provides value configuration key.
     *
     * @return configuration key.
     */
    public String getConfigurationKey() {
        return configurationKey;
    }

    /**
     * Sets value configuration key.
     *
     * @param configurationKey configuration key.
     */
    public void setConfigurationKey(String configurationKey) {
        this.configurationKey = configurationKey;
    }

    /**
     * Provides configuration value.
     *
     * @return configuration value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets configuration value.
     *
     * @param value configuration value.
     */
    public void setValue(String value) {
        this.value = value;
    }


    /**
     * Indicates the value is fetched from the default e.g. {@link Default} annotation.
     *
     * @return {@code true} when the value is fetched from default.
     */
    public boolean isDefaultValue() {
        return defaultValue;
    }

    /**
     * Name of the encryption provider or {@code null} when value is not encrypted.
     *
     * @return encryption provider name.
     */
    public String getEncryptionProvider() {
        return encryptionProvider;
    }

    /**
     * Indicates if property is encrypted.
     *
     * @return {@code true} when value is encrypted.
     */
    public boolean isEncrypted() {
        return encryptionProvider != null;
    }

    /**
     * Sets decrypted configuration value and resets {@code encryptionProvider} to {@code null} to indicate the value
     * is not encrypted anymore.
     *
     * @param value decrypted configuration value.
     * @throws IllegalStateException when value is not encrypted - {@link #isEncrypted()} returns {@code true}.
     */
    public void setDecryptedValue(String value) {
        if (!isEncrypted()) {
            throw new IllegalStateException("Configuration value is not encrypted.");
        }
        this.encryptionProvider = null;
        this.value = value;
    }

    /**
     * Custom meta-data associated with property.
     *
     * @return custom meta-data associated with property or {@code null}.
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Sets custom meta-data associated with property.
     *
     * @param attributes custom meta-data associated with property or {@code null}.
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
