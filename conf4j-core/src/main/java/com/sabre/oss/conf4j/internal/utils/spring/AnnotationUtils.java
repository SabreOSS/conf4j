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

package com.sabre.oss.conf4j.internal.utils.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public abstract class AnnotationUtils {
    /**
     * Get all {@link Annotation Annotations} from the supplied {@link Method}.
     * <p>Correctly handles bridge {@link Method Methods} generated by the compiler.
     *
     * @param method the method to look for annotations on
     * @return the annotations found
     */
    public static Annotation[] getAnnotations(Method method) {
        return BridgeMethodResolver.findBridgedMethod(method).getAnnotations();
    }

    /*
     * Get a single {@link Annotation} of {@code annotationType} from the supplied {@link Method}.
     * <p>Correctly handles bridge {@link Method Methods} generated by the compiler.
     *
     * @param method         the method to look for annotations on
     * @param annotationType the annotation class to look for
     * @param <A> the annotation to look for
     * @return the annotations found
     * @see BridgeMethodResolver#findBridgedMethod(Method)
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
        Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
        A ann = resolvedMethod.getAnnotation(annotationType);
        if (ann == null) {
            for (Annotation metaAnn : resolvedMethod.getAnnotations()) {
                ann = metaAnn.annotationType().getAnnotation(annotationType);
                if (ann != null) {
                    break;
                }
            }
        }
        return ann;
    }

    /**
     * Find a single {@link Annotation} of {@code annotationType} from the supplied {@link Class},
     * traversing its interfaces and superclasses if no annotation can be found on the given class itself.
     * <p>This method explicitly handles class-level annotations which are not declared as
     * {@link java.lang.annotation.Inherited inherited} <i>as well as annotations on interfaces</i>.
     * <p>The algorithm operates as follows: Searches for an annotation on the given class and returns
     * it if found. Else searches all interfaces that the given class declares, returning the annotation
     * from the first matching candidate, if any. Else proceeds with introspection of the superclass
     * of the given class, checking the superclass itself; if no annotation found there, proceeds
     * with the interfaces that the superclass declares. Recursing up through the entire superclass
     * hierarchy if no match is found.
     *
     * @param clazz          the class to look for annotations on
     * @param annotationType the annotation class to look for
     * @param <A>            the annotation to look for
     * @return the annotation found, or {@code null} if none found
     */
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        requireNonNull(clazz, "Class must not be null");
        A annotation = clazz.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            annotation = findAnnotation(ifc, annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        if (!Annotation.class.isAssignableFrom(clazz)) {
            for (Annotation ann : clazz.getAnnotations()) {
                annotation = findAnnotation(ann.annotationType(), annotationType);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass == null || superClass == Object.class) {
            return null;
        }
        return findAnnotation(superClass, annotationType);
    }

    /**
     * Determine whether an annotation of the specified {@code annotationType}
     * is declared locally (i.e., <em>directly present</em>) on the supplied
     * {@code clazz}.
     * <p>The supplied {@link Class} may represent any type.
     * <p>Meta-annotations will <em>not</em> be searched.
     * <p>Note: This method does <strong>not</strong> determine if the annotation
     * is {@linkplain java.lang.annotation.Inherited inherited}.
     *
     * @param annotationType the annotation type to look for
     * @param clazz          the class to check for the annotation on
     * @return {@code true} if an annotation of the specified {@code annotationType}
     * is <em>directly present</em>
     */
    public static boolean isAnnotationDeclaredLocally(Class<? extends Annotation> annotationType, Class<?> clazz) {
        requireNonNull(annotationType, "Annotation type must not be null");
        requireNonNull(clazz, "Class must not be null");
        boolean declaredLocally = false;
        for (Annotation annotation : Arrays.asList(clazz.getDeclaredAnnotations())) {
            if (annotation.annotationType().equals(annotationType)) {
                declaredLocally = true;
                break;
            }
        }
        return declaredLocally;
    }

    /**
     * Retrieve the given annotation's attributes as a Map, preserving all attribute types as-is.
     *
     * @param annotation the annotation to retrieve the attributes for
     * @return the Map of annotation attributes, with attribute names as keys and
     * corresponding attribute values as values
     */
    public static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
        return getAnnotationAttributes(annotation, false);
    }

    /**
     * Retrieve the given annotation's attributes as a Map.
     *
     * @param annotation          the annotation to retrieve the attributes for
     * @param classValuesAsString whether to turn Class references into Strings
     * @return the Map of annotation attributes, with attribute names as keys and
     * corresponding attribute values as values
     */
    private static Map<String, Object> getAnnotationAttributes(Annotation annotation, boolean classValuesAsString) {
        Map<String, Object> attrs = new HashMap<>();
        Method[] methods = annotation.annotationType().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                try {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    Object value = method.invoke(annotation);
                    if (classValuesAsString) {
                        if (value instanceof Class) {
                            value = ((Class<?>) value).getName();
                        } else if (value instanceof Class<?>[]) {
                            Class<?>[] clazzArray = (Class<?>[]) value;
                            String[] newValue = new String[clazzArray.length];
                            for (int i = 0; i < clazzArray.length; i++) {
                                newValue[i] = clazzArray[i].getName();
                            }
                            value = newValue;
                        }
                    }
                    attrs.put(method.getName(), value);
                } catch (Exception ex) {
                    throw new IllegalStateException("Could not obtain annotation attribute values", ex);
                }
            }
        }
        return attrs;
    }

    /**
     * Retrieve the <em>default value</em> of a named Annotation attribute, given the {@link Class annotation type}.
     *
     * @param annotationType the <em>annotation type</em> for which the default value should be retrieved
     * @param attributeName  the name of the attribute value to retrieve.
     * @return the default value of the named attribute, or {@code null} if not found.
     */
    public static Object getDefaultValue(Class<? extends Annotation> annotationType, String attributeName) {
        try {
            Method method = annotationType.getDeclaredMethod(attributeName);
            return method.getDefaultValue();
        } catch (Exception ignore) {
            return null;
        }
    }

}
