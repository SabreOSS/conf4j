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

package com.sabre.oss.conf4j.internal.model.provider.convention;

import com.sabre.oss.conf4j.annotation.Configuration;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.internal.model.provider.MetadataExtractor;
import com.sabre.oss.conf4j.internal.model.provider.MethodsProvider;
import com.sabre.oss.conf4j.internal.model.provider.annotation.AnnotationMetadataExtractor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static com.sabre.oss.conf4j.internal.utils.CachedAnnotationUtils.*;
import static com.sabre.oss.conf4j.internal.utils.PropertyUtils.getPropertyName;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class ConventionMetadataExtractor extends AnnotationMetadataExtractor {
    private static final String ABSTRACT_CONFIGURATION_PREFIX = "Abstract";

    private static final MetadataExtractor INSTANCE = new ConventionMetadataExtractor();

    public static MetadataExtractor getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isConfigurationClass(Class<?> clazz) {
        int modifiers = clazz.getModifiers();

        return !clazz.isPrimitive() &&
                (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers) || !Modifier.isPrivate(modifiers)) &&
                Modifier.isAbstract(modifiers) &&
                !clazz.isAnnotation() &&
                (isClassAnnotatedByAnyAnnotationInPackage(clazz, Configuration.class.getPackage().getName()) || hasAllMethodsConfigurationCompatible(clazz));
    }

    @Override
    public boolean isAbstractConfiguration(Class<?> clazz) {
        return clazz.getSimpleName().startsWith(ABSTRACT_CONFIGURATION_PREFIX) ||
                super.isAbstractConfiguration(clazz);
    }

    @Override
    public boolean isValueConfigurationMethod(Class<?> configurationType, Method method) {
        return isConfigurationPropertyMethod(method) &&
                !isReturnTypeConfiguration(configurationType, method) &&
                !isReturnTypeListOfConfigurations(configurationType, method);
    }

    @Override
    public List<String> getKeys(Class<?> configurationType, Method method) {
        Key keyAnnotation = findMethodAnnotation(configurationType, method, Key.class);
        return keyAnnotation == null ? singletonList(getPropertyName(method)) : super.getKeys(configurationType, method);
    }

    @Override
    public List<String> getPrefixes(Class<?> configurationType) {
        Key keyPrefixAnnotation = findAnnotation(configurationType, Key.class);
        return keyPrefixAnnotation == null ? emptyList() : super.getPrefixes(configurationType);
    }

    /**
     * Check if the class has only methods which makes it configuration compatible.
     * The method must be either concrete (non-abstract) or abstract and follow the rules defined by {@link #isConfigurationPropertyMethod(Method)}.
     * In addition there must be at least one abstract, property method (to avoid cases when an abstract class with all methods
     * implemented is detected).
     *
     * @param clazz - class to check.
     * @return {@code true} when class has only methods which are configuration compatible.
     */
    private boolean hasAllMethodsConfigurationCompatible(Class<?> clazz) {
        boolean configurationPropertyMethodFound = false;
        for (Method method : new MethodsProvider().getAllDeclaredMethods(clazz)) {
            if (Modifier.isAbstract(method.getModifiers()) && !isConfigurationPropertyMethod(method)) {
                return false;
            } else {
                configurationPropertyMethodFound = true;
            }

        }

        return configurationPropertyMethodFound;
    }
}
