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

package com.sabre.oss.conf4j.internal.factory.jdkproxy;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import static org.apache.commons.lang3.SystemUtils.IS_JAVA_1_8;

final class DefaultMethodUtils {
    private static final Function<Class<?>, Lookup> lookupProvider = IS_JAVA_1_8 ? new Jdk8Provider() : new Jdk9Provider();

    private DefaultMethodUtils() {
    }

    static Lookup getLookup(Class<?> declaringClass) {
        return lookupProvider.apply(declaringClass);
    }

    private static class Jdk8Provider implements Function<Class<?>, Lookup> {
        private final Constructor<MethodHandles.Lookup> lookupConstructor = getLookupConstructor();

        @Override
        public Lookup apply(Class<?> clazz) {
            try {
                return lookupConstructor.newInstance(clazz, Lookup.PRIVATE);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Unable to get lookup for " + clazz.getName(), e);
            }
        }

        private Constructor<Lookup> getLookupConstructor() {
            try {
                Constructor<MethodHandles.Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
                return constructor;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to find private Lookup constructor which is necessary to access default methods", e);
            }
        }
    }

    private static class Jdk9Provider implements Function<Class<?>, Lookup> {
        private final Method privateLookupInMethod = getPrivateLookupInMethod();

        @Override
        public Lookup apply(Class<?> clazz) {
            try {
                return (Lookup) privateLookupInMethod.invoke(null, clazz, MethodHandles.lookup());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Unable to get lookup for " + clazz.getName(), e);
            }
        }

        private Method getPrivateLookupInMethod() {
            try {
                return MethodHandles.class.getMethod("privateLookupIn", Class.class, Lookup.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to find private Lookup constructor which is necessary to access default methods", e);
            }
        }
    }


}
