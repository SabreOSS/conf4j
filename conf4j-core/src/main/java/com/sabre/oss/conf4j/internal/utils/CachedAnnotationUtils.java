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

package com.sabre.oss.conf4j.internal.utils;

import com.sabre.oss.conf4j.internal.utils.spring.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableMap;

public final class CachedAnnotationUtils {
    private static final Cache<List<Annotation>> getMethodAnnotationsCache = new Cache<>();
    private static final Cache<Annotation> getMethodAnnotationCache = new Cache<>();
    private static final Cache<Annotation> findMethodAnnotationCache = new Cache<>();
    private static final Cache<Boolean> annotationDeclaredLocallyCache = new Cache<>();
    private static final Cache<? extends Annotation> classAnnotationCache = new Cache<>();
    private static final Cache<Map<String, Object>> annotationAttributesCache = new Cache<>();
    private static final Cache<Annotation> classMethodAnnotationCache = new Cache<>();
    private static final Cache<Boolean> classAnnotatedByAnnotationInPackageCache = new Cache<>();

    private CachedAnnotationUtils() {
        // Do nothing
    }

    public static List<Annotation> getAnnotations(Method method) {
        return getMethodAnnotationsCache.get(() -> Arrays.asList(AnnotationUtils.getAnnotations(method)), method);
    }

    public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
        return annotationType.cast(findMethodAnnotationCache.get(() -> findAnnotationInternal(method, annotationType), method, annotationType));
    }

    public static boolean isAnnotationDeclaredLocally(Class<?> clazz, Class<? extends Annotation> annotationType) {
        Supplier<Boolean> provider = () -> AnnotationUtils.isAnnotationDeclaredLocally(annotationType, clazz);

        return annotationDeclaredLocallyCache.get(provider, annotationType, clazz);
    }

    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        return annotationType.cast(classAnnotationCache.get((Supplier<Annotation>) () -> AnnotationUtils.findAnnotation(clazz, annotationType), clazz, annotationType));
    }

    public static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
        return annotationAttributesCache.get(() -> unmodifiableMap(AnnotationUtils.getAnnotationAttributes(annotation)), annotation);
    }

    private static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
        return annotationType.cast(getMethodAnnotationCache.get(() -> AnnotationUtils.getAnnotation(method, annotationType), method, annotationType));
    }

    public static <A extends Annotation> A findMethodAnnotation(Class<?> clazz, Method method, Class<A> annotationType) {
        return annotationType.cast(classMethodAnnotationCache.get(() -> findMethodAnnotationInternal(clazz, method, annotationType), clazz, method, annotationType));
    }

    public static boolean isClassAnnotatedByAnyAnnotationInPackage(Class<?> configurationClass, String packageName) {
        return classAnnotatedByAnnotationInPackageCache.get(() -> isClassAnnotatedByAnyAnnotationInPackageInternal(configurationClass, packageName), configurationClass, packageName);
    }

    private static <A extends Annotation> A findAnnotationInternal(Method method, Class<A> annotationType) {
        A annotation = getAnnotation(method, annotationType);
        Class<?> cl = method.getDeclaringClass();
        while (annotation == null) {
            cl = cl.getSuperclass();
            if (cl == null || cl == Object.class) {
                break;
            }
            try {
                Method equivalentMethod = cl.getDeclaredMethod(method.getName(), method.getParameterTypes());
                annotation = getAnnotation(equivalentMethod, annotationType);
            } catch (NoSuchMethodException ex) {
                // We're done...
            }
        }
        return annotation;
    }

    private static <A extends Annotation> A findMethodAnnotationInternal(Class<?> clazz, Method method, Class<A> annotationType) {
        A annotation = findAnnotation(method, annotationType);
        if (annotation != null) {
            return annotation;
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            Method newMethod = getMethod(superclass, method);
            if (newMethod != null) {
                annotation = findMethodAnnotationInternal(superclass, newMethod, annotationType);
                if (annotation != null) {
                    return annotation;
                }
            }
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length == 0) {
            return null;
        }
        for (Class<?> i : interfaces) {
            Method newMethod = getMethod(i, method);
            if (newMethod != null) {
                annotation = findMethodAnnotationInternal(i, newMethod, annotationType);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }

    private static Method getMethod(Class<?> clazz, Method method) {
        try {
            return clazz.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    private static boolean isClassAnnotatedByAnyAnnotationInPackageInternal(Class<?> configurationClass, String packageName) {
        for (Annotation annotation : configurationClass.getAnnotations()) {
            if (annotation.annotationType().getPackage().getName().startsWith(packageName)) {
                return true;
            }
        }
        for (Method m : configurationClass.getDeclaredMethods()) {
            for (Annotation a : m.getAnnotations()) {
                if (a.annotationType().getPackage().getName().startsWith(packageName)) {
                    return true;
                }
            }
        }
        Class<?> superclass = configurationClass.getSuperclass();
        if (superclass != null && isClassAnnotatedByAnyAnnotationInPackage(superclass, packageName)) {
            return true;
        }

        for (Class<?> interfaceClass : configurationClass.getInterfaces()) {
            if (isClassAnnotatedByAnyAnnotationInPackage(interfaceClass, packageName)) {
                return true;
            }
        }

        return false;
    }


    private static class Cache<V> {
        private static final Object NULL = new Object();

        private final ConcurrentMap<MultiKey, Object> cache = new ConcurrentHashMap<>();

        public V get(Supplier<? super V> supplier, Object... params) {
            MultiKey key = new MultiKey(params);
            Object value = cache.get(key);
            if (value == null) {
                value = supplier.get();
                cache.putIfAbsent(key, (value != null) ? value : NULL);
            }
            return (V) ((value != NULL) ? value : null);
        }
    }

    private static final class MultiKey {
        private final Object[] keys;
        private final int hashCode;

        private MultiKey(Object[] keys) {
            this.keys = keys;
            this.hashCode = Objects.hash(keys);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other instanceof MultiKey) {
                MultiKey otherMulti = (MultiKey) other;
                return Arrays.equals(this.keys, otherMulti.keys);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

}
