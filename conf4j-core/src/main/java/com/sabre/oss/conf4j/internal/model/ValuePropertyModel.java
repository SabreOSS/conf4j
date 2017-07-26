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

package com.sabre.oss.conf4j.internal.model;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.source.Attributes;
import com.sabre.oss.conf4j.source.OptionalValue;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class ValuePropertyModel extends PropertyModel {
    private final Class<?> declaredType;
    private final List<String> keys;
    private final String fallbackKey;
    private final boolean resetPrefix;
    private final OptionalValue<String> defaultValue;
    private final String encryptionProviderName;
    private final Class<TypeConverter<?>> typeConverterClass;

    public ValuePropertyModel(
            String propertyName, Type type, Class<?> declaredType, Method method, String description,
            List<String> keys, String fallbackKey, boolean resetPrefix, OptionalValue<String> defaultValue,
            String encryptionProviderName, Class<TypeConverter<?>> typeConverterClass,
            Attributes customAttributes) {

        super(propertyName, type, method, description, customAttributes);
        this.declaredType = requireNonNull(declaredType, "declaredType cannot be null");
        this.typeConverterClass = typeConverterClass;
        this.keys = requireNonNull(keys, "keys cannot be null");
        this.fallbackKey = fallbackKey;
        this.resetPrefix = resetPrefix;
        this.defaultValue = defaultValue;
        this.encryptionProviderName = encryptionProviderName;
    }

    public Class<?> getDeclaredType() {
        return declaredType;
    }

    public List<String> getEffectiveKey() {
        return keys;
    }

    public String getFallbackKey() {
        return fallbackKey;
    }

    public boolean isResetPrefix() {
        return resetPrefix;
    }

    public OptionalValue<String> getDefaultValue() {
        return defaultValue;
    }

    public String getEncryptionProviderName() {
        return encryptionProviderName;
    }

    public Class<TypeConverter<?>> getTypeConverterClass() {
        return typeConverterClass;
    }
}
