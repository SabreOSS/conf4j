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
import com.sabre.oss.conf4j.source.OptionalValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Objects.requireNonNull;

public class PropertyMetadata {
    private final String propertyName;
    private final Type type;
    private final Class<? extends TypeConverter<?>> typeConverterClass;
    private final List<String> keySet;
    private final String defaultValue;
    private final boolean defaultValuePresent;
    private final String encryptionProvider;
    private TypeConverter<?> typeConverter;
    private final Map<String, String> attributes;

    public PropertyMetadata(String propertyName, Type type, Class<? extends TypeConverter<?>> typeConverterClass,
                            List<String> keySet, OptionalValue<String> defaultValue, String encryptionProvider,
                            Map<String, String> attributes) {

        this.propertyName = requireNonNull(propertyName, "propertyName cannot be null");
        this.type = type;
        this.keySet = requireNonNull(keySet, "keySet cannot be null");
        this.typeConverterClass = typeConverterClass;
        this.defaultValue = defaultValue.getOrNull();
        this.defaultValuePresent = defaultValue.isPresent();
        this.encryptionProvider = encryptionProvider;
        this.attributes = attributes;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Type getType() {
        return type;
    }

    public Class<? extends TypeConverter<?>> getTypeConverterClass() {
        return typeConverterClass;
    }

    public TypeConverter<?> getTypeConverter() {
        if (typeConverterClass == null) {
            return null;
        }
        if (typeConverter == null) {
            typeConverter = getTypeConverterInstance();
        }
        return typeConverter;
    }

    public List<String> getKeySet() {
        return keySet;
    }

    public OptionalValue<String> getDefaultValue() {
        // OptionalValue is not serializable
        return defaultValuePresent ? present(defaultValue) : absent();
    }

    public String getEncryptionProvider() {
        return encryptionProvider;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    private TypeConverter<?> getTypeConverterInstance() {
        if (typeConverterClass == null) {
            return null;
        }
        try {
            return typeConverterClass.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException("Unable to create type converter instance using parameterless constructor", e);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to create type converter instance, public parameterless constructor not found", e);
        }
    }
}
