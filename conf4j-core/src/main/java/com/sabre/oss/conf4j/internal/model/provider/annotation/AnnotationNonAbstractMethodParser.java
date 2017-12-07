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

package com.sabre.oss.conf4j.internal.model.provider.annotation;

import com.sabre.oss.conf4j.annotation.*;
import com.sabre.oss.conf4j.internal.model.PropertyModel;
import com.sabre.oss.conf4j.internal.model.provider.PropertyMethodParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static com.sabre.oss.conf4j.internal.utils.CachedAnnotationUtils.findAnnotation;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;

class AnnotationNonAbstractMethodParser implements PropertyMethodParser<PropertyModel> {
    // concrete method cannot be annotated with any of conf4j annotations except @Description
    private static final List<Class<? extends Annotation>> forbiddenAnnotations = Arrays.asList(
            Converter.class,
            DefaultsAnnotation.class,
            DefaultSize.class,
            Default.class,
            Encrypted.class,
            FallbackKey.class,
            Key.class,
            IgnorePrefix.class
    );

    @Override
    public boolean applies(Class<?> configurationType, Method method) {
        return !Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public PropertyModel process(Class<?> configurationType, Method method) {
        checkAnnotations(method);
        return null;
    }

    private void checkAnnotations(Method method) {
        List<String> unexpectedAnnotations = forbiddenAnnotations.stream()
                .filter(a -> findAnnotation(method, a) != null)
                .map(a -> '@' + a.getClass().getSimpleName())
                .collect(toList());
        if (!unexpectedAnnotations.isEmpty()) {
            throw new IllegalArgumentException(
                    format("Method %s is non abstract and annotated with not allowed annotation(s): %s.",
                            method, join(unexpectedAnnotations, ",")));
        }
    }
}
