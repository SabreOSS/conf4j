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

import com.sabre.oss.conf4j.internal.utils.spring.ConcurrentReferenceHashMap;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static java.beans.Introspector.decapitalize;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;

/**
 * Set of helper methods to access JavaBean properties by name.
 */
public final class PropertyUtils {
    /**
     * An indicator used in {@link #propertyDescriptorCache} in case the property for the given class is not found
     */
    private static final PropertyDescriptor NOT_FOUND_PROPERTY_DESCRIPTOR_INDICATOR;
    /**
     * Property descriptors cache.
     */
    private static final ConcurrentMap<ClassNameKey, PropertyDescriptor> propertyDescriptorCache = new ConcurrentReferenceHashMap<>();

    static {
        try {
            NOT_FOUND_PROPERTY_DESCRIPTOR_INDICATOR = new PropertyDescriptor("notFound", NotFoundPropertyDescriptorHelper.class);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Unable to create 'not found property descriptor' indicator ", e);
        }
    }

    private PropertyUtils() {
    }

    /**
     * Provides property name from getter or setter.
     *
     * @param method method must be either getter or setter
     * @return property name
     * @throws NullPointerException     when {@code method} is {@code null}.
     * @throws IllegalArgumentException when {@code method} is not getter nor setter.
     */
    public static String getPropertyName(Method method) {
        requireNonNull(method, "method cannot be null");

        String name = method.getName();
        if (name.startsWith("set")) {
            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("Method " + method + " is not a valid setter.");
            }
            name = name.substring(3);
        } else if (name.startsWith("get") || name.startsWith("is")) {
            if (method.getParameterCount() != 0 || void.class.equals(method.getReturnType())) {
                throw new IllegalArgumentException("Method " + method + " is not a valid getter.");
            }
            name = name.substring(name.startsWith("get") ? 3 : 2);
        } else {
            throw new IllegalArgumentException("Method " + name + " is not a valid getter nor setter.");
        }

        return decapitalize(name);
    }

    /**
     * Gets bean property.
     *
     * @param bean target object the property is got.
     * @param name property name.
     * @return property value.
     * @throws NullPointerException     when {@code bean} or {@code name} is {@code null}.
     * @throws IllegalArgumentException when property cannot be found, the access the the method.
     * @throws RuntimeException         (or any other exception that extends is) thrown from the setter.
     */
    public static Object getProperty(Object bean, String name) {
        requireNonNull(bean, "bean cannot be null");
        requireNonNull(name, "name cannot be null");

        Class<?> beanClass = bean.getClass();
        try {
            PropertyDescriptor descriptor = findPropertyDescriptor(beanClass, name);
            Method readMethod = descriptor.getReadMethod();

            return readMethod.invoke(bean, EMPTY_OBJECT_ARRAY);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to get property " + name + " on " + bean.getClass(), e);
        }
    }

    /**
     * Sets bean property.
     *
     * @param bean  target object the property is set.
     * @param name  property name.
     * @param value value to set.
     * @throws NullPointerException     when {@code bean} or {@code name} is {@code null}.
     * @throws IllegalArgumentException when property cannot be found, the access method.
     * @throws RuntimeException         (or any other exception that extends is) thrown from the setter.
     */
    public static void setProperty(Object bean, String name, Object value) {
        requireNonNull(bean, "bean cannot be null");
        requireNonNull(name, "name cannot be null");

        Class<?> beanClass = bean.getClass();
        try {
            PropertyDescriptor descriptor = findPropertyDescriptor(beanClass, name);
            Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod == null) {
                throw new IllegalArgumentException("Unable to set property '" + name + " on " + bean.getClass() + " because setter of type " + descriptor.getPropertyType() + " is not available");
            }
            writeMethod.invoke(bean, value);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to set property " + name + " on " + bean.getClass(), e);
        }
    }

    private static PropertyDescriptor findPropertyDescriptor(Class<?> beanClass, String name) throws NoSuchMethodException {
        ClassNameKey key = new ClassNameKey(beanClass, name);

        PropertyDescriptor propertyDescriptor = propertyDescriptorCache.computeIfAbsent(key, k -> {
            BeanInfo beanInfo;
            try {
                beanInfo = Introspector.getBeanInfo(k.getClazz());
            } catch (IntrospectionException e) {
                throw new IllegalStateException("Unable to introspect class '" + k.getClazz() + '\'', e);
            }

            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (Objects.equals(name, pd.getName())) {
                    return pd;
                }
            }
            return NOT_FOUND_PROPERTY_DESCRIPTOR_INDICATOR;
        });

        if (propertyDescriptor == NOT_FOUND_PROPERTY_DESCRIPTOR_INDICATOR) {
            throw new NoSuchMethodException("Unknown property '" + name + "' on class '" + beanClass + '\'');
        }

        return propertyDescriptor;
    }

    private static final class ClassNameKey {
        private final Class<?> clazz;
        private final String name;
        private final int hashCode;

        private ClassNameKey(Class<?> clazz, String name) {
            this.clazz = clazz;
            this.name = name;
            this.hashCode = calcHashCode(clazz, name);
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClassNameKey that = (ClassNameKey) o;
            return clazz == that.clazz &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        private int calcHashCode(Class<?> clazz, String name) {
            int result = clazz.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    /**
     * Just a fake class used to create an in indicator in the cache the property descriptor is not found.
     */
    private static class NotFoundPropertyDescriptorHelper {
        public boolean isNotFound() {
            throw new IllegalStateException("Don't call me");
        }

        public void setNotFound(boolean notFound) {
            throw new IllegalStateException("Don't call me");
        }
    }

}
