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

package com.sabre.oss.conf4j.internal.model.provider;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class MethodsProvider {
    public Collection<Method> getAllDeclaredMethods(Class<?> configurationType) {
        Set<MethodWrapper> methods = new LinkedHashSet<>();

        Class<?> type = configurationType;
        methods.addAll(addMethods(type.getMethods()));
        if (type.getSuperclass() != null) {
            do {
                methods.addAll(addMethods(type.getDeclaredMethods()));
                type = type.getSuperclass();
            }
            while (!Object.class.equals(type));
        }

        return methods.stream()
                .map(MethodWrapper::getMethod)
                .collect(toList());
    }

    private Collection<MethodWrapper> addMethods(Method[] methods) {
        Map<MethodWrapper, MethodWrapper> methodWrappers = new LinkedHashMap<>();
        for (Method m : methods) {
            MethodWrapper wrapper = new MethodWrapper(m);
            addIfNewMethodOrOverride(methodWrappers, wrapper);
        }
        return methodWrappers.values();
    }

    private void addIfNewMethodOrOverride(Map<MethodWrapper, MethodWrapper> methodWrappers, MethodWrapper wrapper) {
        if (methodWrappers.containsKey(wrapper)) {
            if (isMethodAnOverride(methodWrappers, wrapper)) {
                methodWrappers.put(wrapper, wrapper);
            }
        } else {
            methodWrappers.put(wrapper, wrapper);
        }
    }

    private boolean isMethodAnOverride(Map<MethodWrapper, MethodWrapper> methodWrappers, MethodWrapper wrapper) {
        MethodWrapper oldWrapper = methodWrappers.get(wrapper);
        if (oldWrapper != null) {
            if (oldWrapper.getMethod().getReturnType().isAssignableFrom(wrapper.getMethod().getReturnType())) {
                return true;
            }
        }
        return false;
    }

    static class MethodWrapper {
        private final Method method;

        protected MethodWrapper(Method method) {
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MethodWrapper)) {
                return false;
            }
            MethodWrapper rhs = (MethodWrapper) obj;

            return Objects.equals(method.getName(), rhs.method.getName()) &&
                    Arrays.equals(method.getParameterTypes(), rhs.method.getParameterTypes()) &&
                    returnTypesAreCovariant(method, rhs);
        }

        @Override
        public int hashCode() {
            return method.getName().hashCode();
        }

        private boolean returnTypesAreCovariant(Method method, MethodWrapper rhs) {
            return rhs.method.getReturnType().isAssignableFrom(method.getReturnType()) ||
                    method.getReturnType().isAssignableFrom(rhs.method.getReturnType());
        }
    }

}
