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

package com.sabre.oss.conf4j.internal.utils.spring;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

abstract class BridgeMethodResolver {
    static Method findBridgedMethod(Method bridgeMethod) {
        if (bridgeMethod == null || !bridgeMethod.isBridge()) {
            return bridgeMethod;
        }
        // Gather all methods with matching name and parameter size.
        List<Method> candidateMethods = new ArrayList<>();
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bridgeMethod.getDeclaringClass());
        for (Method candidateMethod : methods) {
            if (isBridgedCandidateFor(candidateMethod, bridgeMethod)) {
                candidateMethods.add(candidateMethod);
            }
        }
        // Now perform simple quick check.
        if (candidateMethods.size() == 1) {
            return candidateMethods.get(0);
        }
        // Search for candidate match.
        Method bridgedMethod = searchCandidates(candidateMethods, bridgeMethod);
        if (bridgedMethod != null) {
            // Bridged method found...
            return bridgedMethod;
        } else {
            // A bridge method was passed in but we couldn't find the bridged method.
            // Let's proceed with the passed-in method and hope for the best...
            return bridgeMethod;
        }
    }

    private static Method searchCandidates(List<Method> candidateMethods, Method bridgeMethod) {
        if (candidateMethods.isEmpty()) {
            return null;
        }
        Map<TypeVariable<?>, Type> typeParameterMap = GenericTypeResolver.getTypeVariableMap(bridgeMethod.getDeclaringClass());
        Method previousMethod = null;
        boolean sameSig = true;
        for (Method candidateMethod : candidateMethods) {
            if (isBridgeMethodFor(bridgeMethod, candidateMethod, typeParameterMap)) {
                return candidateMethod;
            }
            if (previousMethod != null) {
                sameSig = sameSig &&
                        Arrays.equals(candidateMethod.getGenericParameterTypes(), previousMethod.getGenericParameterTypes());
            }
            previousMethod = candidateMethod;
        }
        return (sameSig ? candidateMethods.get(0) : null);
    }

    private static boolean isBridgedCandidateFor(Method candidateMethod, Method bridgeMethod) {
        return (!candidateMethod.isBridge() && !candidateMethod.equals(bridgeMethod) &&
                candidateMethod.getName().equals(bridgeMethod.getName()) &&
                candidateMethod.getParameterTypes().length == bridgeMethod.getParameterTypes().length);
    }

    private static boolean isBridgeMethodFor(Method bridgeMethod, Method candidateMethod, Map<TypeVariable<?>, Type> typeVariableMap) {
        if (isResolvedTypeMatch(candidateMethod, bridgeMethod, typeVariableMap)) {
            return true;
        }
        Method method = findGenericDeclaration(bridgeMethod);
        return (method != null && isResolvedTypeMatch(method, candidateMethod, typeVariableMap));
    }

    private static Method findGenericDeclaration(Method bridgeMethod) {
        // Search parent types for method that has same signature as bridge.
        Class<?> superclass = bridgeMethod.getDeclaringClass().getSuperclass();
        while (!Object.class.equals(superclass)) {
            Method method = searchForMatch(superclass, bridgeMethod);
            if (method != null && !method.isBridge()) {
                return method;
            }
            superclass = superclass.getSuperclass();
        }

        // Search interfaces.
        Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(bridgeMethod.getDeclaringClass());
        for (Class<?> ifc : interfaces) {
            Method method = searchForMatch(ifc, bridgeMethod);
            if (method != null && !method.isBridge()) {
                return method;
            }
        }

        return null;
    }

    private static boolean isResolvedTypeMatch(
            Method genericMethod, Method candidateMethod, Map<TypeVariable<?>, Type> typeVariableMap) {

        Type[] genericParameters = genericMethod.getGenericParameterTypes();
        Class<?>[] candidateParameters = candidateMethod.getParameterTypes();
        if (genericParameters.length != candidateParameters.length) {
            return false;
        }
        for (int i = 0; i < genericParameters.length; i++) {
            Type genericParameter = genericParameters[i];
            Class<?> candidateParameter = candidateParameters[i];
            if (candidateParameter.isArray()) {
                // An array type: compare the component type.
                Type rawType = GenericTypeResolver.getRawType(genericParameter, typeVariableMap);
                if (rawType instanceof GenericArrayType) {
                    if (!candidateParameter.getComponentType().equals(
                            GenericTypeResolver.resolveType(((GenericArrayType) rawType).getGenericComponentType(), typeVariableMap))) {
                        return false;
                    }
                    break;
                }
            }
            // A non-array type: compare the type itself.
            Class<?> resolvedParameter = GenericTypeResolver.resolveType(genericParameter, typeVariableMap);
            if (!candidateParameter.equals(resolvedParameter)) {
                return false;
            }
        }
        return true;
    }

    private static Method searchForMatch(Class<?> type, Method bridgeMethod) {
        return ReflectionUtils.findMethod(type, bridgeMethod.getName(), bridgeMethod.getParameterTypes());
    }

}
