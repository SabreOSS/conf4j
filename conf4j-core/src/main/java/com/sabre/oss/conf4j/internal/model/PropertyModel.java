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

import com.sabre.oss.conf4j.source.Attributes;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

public abstract class PropertyModel {
    protected ConfigurationModel owner;
    protected final String propertyName;
    protected final Type type;
    protected final Method method;
    protected final String description;
    protected final Attributes customAttributes;

    protected PropertyModel(String propertyName, Type type, Method method, String description, Attributes customAttributes) {
        this.propertyName = requireNonNull(propertyName, "propertyName cannot be null");
        this.type = requireNonNull(type, "type cannot be null");
        this.method = requireNonNull(method, "method cannot be null");
        this.description = description;
        this.customAttributes = customAttributes;
    }

    public ConfigurationModel getOwner() {
        return owner;
    }

    void setOwner(ConfigurationModel owner) {
        this.owner = owner;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Type getType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public String getDescription() {
        return description;
    }

    public Attributes getCustomAttributes() {
        return customAttributes;
    }
}
