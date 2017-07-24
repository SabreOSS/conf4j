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

package com.sabre.oss.conf4j.internal.model.provider.annotation;

import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.internal.model.PropertyModel;
import com.sabre.oss.conf4j.internal.model.provider.PropertyMethodParser;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static java.lang.String.format;

class UnannotatedAbstractPropertyMethodParser implements PropertyMethodParser<PropertyModel> {
    @Override
    public boolean applies(Class<?> configurationType, Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public PropertyModel process(Class<?> configurationType, Method method) {
        throw new IllegalArgumentException(format("%s is an abstract method but it is not a valid configuration property. " +
                        "Configuration property method must be abstract, public or protected, without parameters and " +
                        "its name starts with get or is (if return type is boolean). The return type cannot be void. " +
                        "If it doesn't return sub-configuration or list of sub-configurations, it must be annotated with @%s.",
                method.toString(), Key.class.getSimpleName()));
    }
}
