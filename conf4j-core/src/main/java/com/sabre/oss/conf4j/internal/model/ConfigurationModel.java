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

package com.sabre.oss.conf4j.internal.model;

import com.sabre.oss.conf4j.source.Attributes;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class ConfigurationModel {
    private final Class<?> configurationType;
    private final String description;
    private final boolean abstractConfiguration;
    private final List<String> prefixes;
    private final Attributes customAttributes;
    private final List<PropertyModel> properties;

    public ConfigurationModel(Class<?> configurationType, String description, boolean abstractConfiguration,
                              List<String> prefixes, Attributes customAttributes, List<PropertyModel> properties) {
        this.configurationType = requireNonNull(configurationType, "configurationType cannot be null");
        this.description = description;
        this.abstractConfiguration = abstractConfiguration;
        this.prefixes = requireNonNull(prefixes, "prefixes cannot be null");
        this.properties = requireNonNull(properties, "properties cannot be null");
        this.customAttributes = customAttributes;
        this.properties.forEach(p -> p.setOwner(this));
    }

    public Class<?> getConfigurationType() {
        return configurationType;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAbstractConfiguration() {
        return abstractConfiguration;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public Attributes getCustomAttributes() {
        return customAttributes;
    }

    public List<PropertyModel> getProperties() {
        return properties;
    }
}
