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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SubConfigurationPropertyModel extends PropertyModel {
    private final ConfigurationModel typeModel;
    private final Class<?> declaredType;
    private final List<String> prefixes;
    private final boolean resetPrefix;
    private final String fallbackKey;
    private final Map<String, String> defaultValues;

    public SubConfigurationPropertyModel(
            String propertyName, Class<?> type, Class<?> declaredType, ConfigurationModel typeModel, Method method, String description,
            List<String> prefixes, boolean resetPrefix,
            String fallbackKey, Map<String, String> defaultValues,
            Map<String, String> attributes) {

        super(propertyName, type, method, description, attributes);
        this.typeModel = requireNonNull(typeModel, "typeModel cannot be null");
        this.declaredType = requireNonNull(declaredType, "declaredType cannot be null");
        this.prefixes = requireNonNull(prefixes, "prefixes cannot be null");
        this.resetPrefix = resetPrefix;
        this.fallbackKey = fallbackKey;
        this.defaultValues = requireNonNull(defaultValues, "defaultValues cannot be null");
    }

    @Override
    public Class<?> getType() {
        return (Class<?>) super.getType();
    }

    public ConfigurationModel getTypeModel() {
        return typeModel;
    }

    public Class<?> getDeclaredType() {
        return declaredType;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public boolean isResetPrefix() {
        return resetPrefix;
    }

    public String getFallbackKey() {
        return fallbackKey;
    }

    public Map<String, String> getDefaultValues() {
        return defaultValues;
    }
}
