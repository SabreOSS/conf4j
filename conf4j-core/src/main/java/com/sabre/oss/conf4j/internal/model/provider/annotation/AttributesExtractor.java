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

import com.sabre.oss.conf4j.annotation.Meta;
import com.sabre.oss.conf4j.annotation.Meta.Metas;
import com.sabre.oss.conf4j.internal.utils.AttributesUtils;
import com.sabre.oss.conf4j.internal.utils.CachedAnnotationUtils;
import com.sabre.oss.conf4j.internal.utils.spring.ConcurrentReferenceHashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static com.sabre.oss.conf4j.internal.utils.CachedAnnotationUtils.getAnnotationAttributes;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNoneEmpty;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Extracts custom attributes from {@code @Meta} and {@code Metas} annotations or from custom annotations
 * meta-annotated by {@code @Meta}.
 *
 * @see Meta
 */
final class AttributesExtractor {
    private static final Map<Class<?>, Map<String, String>> attributesByTypeCache = new ConcurrentReferenceHashMap<>();
    private static final Map<Method, Map<String, String>> attributesByMethodCache = new ConcurrentReferenceHashMap<>();
    private static final Map<Class<?>, List<Annotation>> allAnnotationsByTypeCache = new ConcurrentReferenceHashMap<>();
    private static final Map<Method, List<Annotation>> allAnnotationsByMethodCache = new ConcurrentReferenceHashMap<>();

    private AttributesExtractor() {
    }

    /**
     * Extracts meta-attributes for supplied {@link Class}.
     * It searches for annotations in the class, all supper classes and all interfaces.
     *
     * @param clazz the method to look for meta-attributes on.
     * @return meta-attributes associated with class.
     */
    static Map<String, String> getMetaAttributes(Class<?> clazz) {
        requireNonNull(clazz, "clazz cannot be null");
        return attributesByTypeCache.computeIfAbsent(clazz, c -> getMetaAttributes(getAnnotations(c)));
    }

    /**
     * Extracts meta-attributes for supplied {@link Method}.
     * <p>
     * Correctly handles bridge {@link Method Methods} generated by the compiler.
     *
     * @param method the method to look for meta-attributes on.
     * @return meta-attributes associated with method.
     */
    static Map<String, String> getMetaAttributes(Method method) {
        requireNonNull(method, "method cannot be null");
        return attributesByMethodCache.computeIfAbsent(method, c -> getMetaAttributes(getAnnotations(c)));
    }

    private static Map<String, String> getMetaAttributes(List<Annotation> annotations) {
        Map<String, String> attributes = annotations.stream()
                .flatMap(a -> extractMeta(a).stream())
                .collect(toMap(Meta::name, Meta::value, (p, n) -> n, LinkedHashMap::new));
        return attributes.isEmpty() ? null : AttributesUtils.attributes(attributes);
    }

    private static List<Meta> extractMeta(Annotation annotation) {
        if (annotation instanceof Meta) {
            return singletonList((Meta) annotation);
        }

        if (annotation instanceof Metas) {
            return asList(((Metas) annotation).value());
        }

        Metas metas = annotation.annotationType().getAnnotation(Metas.class);
        if (metas != null) {
            // annotation is annotated with @Metas or multiple @Meta - annotation cannot define any attribute
            Map<String, Object> attributes = getAnnotationAttributes(annotation);
            if (!attributes.isEmpty()) {
                throw new IllegalArgumentException(
                        format("%s annotation is meta-annotated with @%s cannot define any attribute, but following are defined: %s ",
                                annotation, Metas.class.getName(), join(attributes.keySet(), ", "))
                );
            }

            return asList(metas.value());
        }

        Meta meta = annotation.annotationType().getAnnotation(Meta.class);
        if (meta != null) {
            // annotation is annotated with one @Meta
            // There are two cases:
            // - key and value are set; annotation cannot define any attribute.
            // - only key is set; the value is provided by an attribute; annotation must define exactly one attribute
            //   and its type cannot be array nor annotation nor class
            Map<String, Object> attributes = getAnnotationAttributes(annotation);
            if (attributes.size() > 1) {
                throw new IllegalArgumentException(
                        format("%s annotation is meta-annotated with @%s and define more than one attribute: %s ",
                                annotation, Meta.class.getName(), join(attributes.keySet(), ", "))
                );
            }

            if (attributes.isEmpty()) {
                return singletonList(meta);
            } else {
                Map.Entry<String, Object> entry = attributes.entrySet().iterator().next();
                Object annotationValue = entry.getValue();
                if (annotationValue instanceof Annotation || annotationValue instanceof Class || annotationValue.getClass().isArray()) {
                    throw new IllegalArgumentException(
                            format("%s annotation is meta-annotated with @%s and its attribute '%s' type is %s. Only scalar, simple types are supported.",
                                    annotation, Meta.class.getName(), entry.getKey(), annotationValue.getClass().getName())
                    );
                }
                String value = annotationValue.toString();

                return singletonList(new MetaProxy(meta.name(), value));
            }
        }

        // annotation IS NOT annotated with @Meta, but its attributes can be annotated.
        // In such case:
        // - all attributes must be annotated with exactly one @Meta,
        // - name is provided by @Meta
        // - value is provided by the attribute's value.
        Method[] methods = annotation.annotationType().getDeclaredMethods();
        int attributeMethods = 0;
        int metaAnnotatedMethods = 0;
        List<Meta> metaList = new ArrayList<>();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                attributeMethods++;
                meta = method.getAnnotation(Meta.class);
                if (meta != null) {
                    metaAnnotatedMethods++;
                    String name = meta.name();
                    String value = meta.value();
                    if (isNoneEmpty(value)) {
                        throw new IllegalArgumentException(
                                format("@%s annotation attribute %s is annotated with @%s and value '%s' is provided.",
                                        annotation.getClass().getName(), method.getName(), Meta.class.getName(), value)
                        );
                    }
                    try {
                        if (!method.isAccessible()) {
                            method.setAccessible(true);
                        }
                        Object annotationValue = method.invoke(annotation);
                        if (annotationValue instanceof Annotation || annotationValue instanceof Class || annotationValue.getClass().isArray()) {
                            throw new IllegalArgumentException(
                                    format("@%s annotation attribute %s is annotated with @%s and its type is %s. Only scalar, simple types are supported.",
                                            annotation.getClass().getName(), method.getName(), Meta.class.getName(), annotationValue.getClass().getName())
                            );
                        }
                        value = annotationValue.toString();
                    } catch (Exception e) {
                        throw new IllegalStateException("Could not obtain annotation attribute values", e);
                    }
                    metaList.add(new MetaProxy(name, value));
                }
            }
        }

        if (metaAnnotatedMethods >= 1 && metaAnnotatedMethods != attributeMethods) {
            throw new IllegalArgumentException(
                    format("All %s annotations attributes must be annotated with @%s.",
                            annotation, Meta.class.getName())
            );
        }

        return metaList;

    }

    private static List<Annotation> getAnnotations(Class<?> clazz) {
        return allAnnotationsByTypeCache.computeIfAbsent(clazz, AttributesExtractor::getAnnotationsInternal);
    }

    private static List<Annotation> getAnnotationsInternal(Class<?> clazz) {
        List<Annotation[]> annotationsList = new ArrayList<>();
        collectAnnotations(clazz, annotationsList);
        reverse(annotationsList);
        List<Annotation> annotations = new ArrayList<>();
        for (Annotation[] annos : annotationsList) {
            Collections.addAll(annotations, annos);
        }
        return annotations;
    }

    private static List<Annotation> getAnnotations(Method method) {
        return allAnnotationsByMethodCache.computeIfAbsent(method, AttributesExtractor::getAnnotationsInternal);
    }

    private static List<Annotation> getAnnotationsInternal(Method method) {
        List<Annotation> annotations = CachedAnnotationUtils.getAnnotations(method);
        Class<?> cl = method.getDeclaringClass();
        while (true) {
            cl = cl.getSuperclass();
            if (cl == null || cl == Object.class) {
                break;
            }
            try {
                Method equivalentMethod = cl.getDeclaredMethod(method.getName(), method.getParameterTypes());
                annotations.addAll(CachedAnnotationUtils.getAnnotations(equivalentMethod));
            } catch (NoSuchMethodException ex) {
                // We're done...
            }
        }

        reverse(annotations);

        return annotations;
    }

    private static void collectAnnotations(Class<?> clazz, List<Annotation[]> annotationsList) {
        requireNonNull(clazz, "Class must not be null");

        Annotation[] annotations = clazz.getAnnotations();
        if (annotations != null) {
            annotationsList.add(annotations);
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            collectAnnotations(ifc, annotationsList);
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass == null || superClass == Object.class) {
            return;
        }
        collectAnnotations(superClass, annotationsList);
    }

    private static final class MetaProxy implements Meta {
        private final String name;
        private final String value;

        private MetaProxy(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Meta.class;
        }
    }
}
