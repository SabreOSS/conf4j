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

import com.sabre.oss.conf4j.annotation.Configuration;
import com.sabre.oss.conf4j.internal.model.PropertyModel;
import com.sabre.oss.conf4j.internal.model.provider.MetadataExtractor;
import com.sabre.oss.conf4j.internal.model.provider.PropertyMethodParser;
import com.sabre.oss.conf4j.internal.utils.CachedAnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;

abstract class AbstractAnnotationPropertyMethodParser<T extends PropertyModel> implements PropertyMethodParser<T> {
    private static final Package conf4jPackage = Configuration.class.getPackage();
    protected final MetadataExtractor metadataExtractor;

    AbstractAnnotationPropertyMethodParser(MetadataExtractor metadataExtractor) {
        this.metadataExtractor = requireNonNull(metadataExtractor, "metadataExtractor cannot be null");
    }

    protected void checkAllowedAnnotations(Method method, Collection<Class<? extends Annotation>> allowedAnnotations) {
        List<Annotation> annotations = CachedAnnotationUtils.getAnnotations(method);

        List<String> unexpectedAnnotations = annotations.stream()
                .map(Annotation::annotationType)
                .filter(a -> conf4jPackage.equals(a.getPackage()))
                .filter(a -> !allowedAnnotations.contains(a))
                .map(a -> '@' + a.getSimpleName())
                .collect(toList());

        if (!unexpectedAnnotations.isEmpty()) {
            throw new IllegalArgumentException(
                    format("%s method is annotated with disallowed annotation(s): %s.",
                            method, join(unexpectedAnnotations, ",")));
        }
    }
}
