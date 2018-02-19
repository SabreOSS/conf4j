/*
 * MIT License
 *
 * Copyright 2017-2018 Sabre GLBL Inc.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    /**
     * Creates class instances using public, parameter-less constructor.
     *
     * @param clazz the class which should be instantiated. It must provide a public, parameter-less constructor.
     * @param <T>   the type of the class
     * @return created instance
     * @throws NullPointerException when {@code clazz} is null.
     * @see Class#getDeclaredConstructor(Class[])
     * @see Constructor#newInstance(Object...)
     */
    public static <T> T createInstance(Class<T> clazz) {
        requireNonNull(clazz, "clazz cannot be null");

        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides default value for class. It is intended to provide default values for primitive types, for
     * other types it returns {@code null}.
     *
     * @param clazz the class for which a default value is needed.
     * @return A default value for the given class (the boxed default value for primitives, {@code null} otherwise).
     * @throws NullPointerException when {@code clazz} is {@code null};
     */
    public static Object getDefaultValue(Class<?> clazz) {
        requireNonNull(clazz, "clazz cannot be null");
        return DefaultPrimitiveValues.DEFAULT_VALUES.get(clazz);
    }

    private static final class DefaultPrimitiveValues {
        private boolean b;
        private byte by;
        private char c;
        private double d;
        private float f;
        private int i;
        private long l;
        private short s;

        private static final Map<Class<?>, Object> DEFAULT_VALUES = new HashMap<>();

        static {
            DefaultPrimitiveValues instance = new DefaultPrimitiveValues();
            for (final Field field : DefaultPrimitiveValues.class.getDeclaredFields()) {
                try {
                    DEFAULT_VALUES.put(field.getType(), field.get(instance));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
