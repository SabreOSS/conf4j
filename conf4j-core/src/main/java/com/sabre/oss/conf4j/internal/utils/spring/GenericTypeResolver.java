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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.requireNonNull;

public abstract class GenericTypeResolver {
    private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> typeVariableCache = synchronizedMap(new WeakHashMap<>());

    public static Class<?> resolveReturnType(Method method, Class<?> clazz) {
        requireNonNull(method, "Method must not be null");
        requireNonNull(clazz, "Class must not be null");

        Type genericType = method.getGenericReturnType();
        Map<TypeVariable<?>, Type> typeVariableMap = getTypeVariableMap(clazz);
        Type rawType = getRawType(genericType, typeVariableMap);
        return (rawType instanceof Class ? (Class<?>) rawType : method.getReturnType());
    }

    public static Class<?> resolveType(Type genericType, Map<TypeVariable<?>, Type> typeVariableMap) {
        Type rawType = getRawType(genericType, typeVariableMap);
        return (rawType instanceof Class ? (Class<?>) rawType : Object.class);
    }

    static Type getRawType(Type genericType, Map<TypeVariable<?>, Type> typeVariableMap) {
        Type resolvedType = genericType;
        if (genericType instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) genericType;
            resolvedType = typeVariableMap.get(tv);
            if (resolvedType == null) {
                resolvedType = extractBoundForTypeVariable(tv);
            }
        }
        if (resolvedType instanceof ParameterizedType) {
            return ((ParameterizedType) resolvedType).getRawType();
        } else {
            return resolvedType;
        }
    }

    public static Map<TypeVariable<?>, Type> getTypeVariableMap(Class<?> clazz) {
        Reference<Map<TypeVariable<?>, Type>> ref = typeVariableCache.get(clazz);
        Map<TypeVariable<?>, Type> typeVariableMap = (ref != null ? ref.get() : null);

        if (typeVariableMap == null) {
            typeVariableMap = new HashMap<>();

            // interfaces
            extractTypeVariablesFromGenericInterfaces(clazz.getGenericInterfaces(), typeVariableMap);

            // super class
            Type genericType = clazz.getGenericSuperclass();
            Class<?> type = clazz.getSuperclass();
            while (type != null && !Object.class.equals(type)) {
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericType;
                    populateTypeMapFromParameterizedType(pt, typeVariableMap);
                }
                extractTypeVariablesFromGenericInterfaces(type.getGenericInterfaces(), typeVariableMap);
                genericType = type.getGenericSuperclass();
                type = type.getSuperclass();
            }

            // enclosing class
            type = clazz;
            while (type.isMemberClass()) {
                genericType = type.getGenericSuperclass();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericType;
                    populateTypeMapFromParameterizedType(pt, typeVariableMap);
                }
                type = type.getEnclosingClass();
            }

            typeVariableCache.put(clazz, new WeakReference<>(typeVariableMap));
        }

        return typeVariableMap;
    }

    private static Class<?>[] doResolveTypeArguments(Class<?> ownerClass, Class<?> classToIntrospect, Class<?> genericIfc) {
        Class<?> currentClass = classToIntrospect;
        while (currentClass != null) {
            if (genericIfc.isInterface()) {
                Type[] ifcs = currentClass.getGenericInterfaces();
                for (Type ifc : ifcs) {
                    Class<?>[] result = doResolveTypeArguments(ownerClass, ifc, genericIfc);
                    if (result != null) {
                        return result;
                    }
                }
            } else {
                Class<?>[] result = doResolveTypeArguments(
                        ownerClass, currentClass.getGenericSuperclass(), genericIfc);
                if (result != null) {
                    return result;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    private static Class<?>[] doResolveTypeArguments(Class<?> ownerClass, Type ifc, Class<?> genericIfc) {
        if (ifc instanceof ParameterizedType) {
            ParameterizedType paramIfc = (ParameterizedType) ifc;
            Type rawType = paramIfc.getRawType();
            if (genericIfc.equals(rawType)) {
                Type[] typeArgs = paramIfc.getActualTypeArguments();
                Class<?>[] result = new Class[typeArgs.length];
                for (int i = 0; i < typeArgs.length; i++) {
                    Type arg = typeArgs[i];
                    result[i] = extractClass(ownerClass, arg);
                }
                return result;
            } else if (genericIfc.isAssignableFrom((Class<?>) rawType)) {
                return doResolveTypeArguments(ownerClass, (Class<?>) rawType, genericIfc);
            }
        } else if (genericIfc.isAssignableFrom((Class<?>) ifc)) {
            return doResolveTypeArguments(ownerClass, (Class<?>) ifc, genericIfc);
        }
        return null;
    }

    /**
     * Extract a class instance from given Type.
     */
    private static Class<?> extractClass(Class<?> ownerClass, Type arg) {
        Type type = arg;
        if (type instanceof ParameterizedType) {
            return extractClass(ownerClass, ((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Type gt = gat.getGenericComponentType();
            Class<?> componentClass = extractClass(ownerClass, gt);
            return Array.newInstance(componentClass, 0).getClass();
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            type = getTypeVariableMap(ownerClass).get(tv);
            if (type == null) {
                type = extractBoundForTypeVariable(tv);
            } else {
                type = extractClass(ownerClass, type);
            }
        }
        return (type instanceof Class ? (Class<?>) type : Object.class);
    }


    private static Type extractBoundForTypeVariable(TypeVariable<?> typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Object.class;
        }
        Type bound = bounds[0];
        if (bound instanceof TypeVariable) {
            bound = extractBoundForTypeVariable((TypeVariable<?>) bound);
        }
        return bound;
    }

    private static void extractTypeVariablesFromGenericInterfaces(Type[] genericInterfaces, Map<TypeVariable<?>, Type> typeVariableMap) {
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                populateTypeMapFromParameterizedType(pt, typeVariableMap);
                if (pt.getRawType() instanceof Class) {
                    extractTypeVariablesFromGenericInterfaces(
                            ((Class<?>) pt.getRawType()).getGenericInterfaces(), typeVariableMap);
                }
            } else if (genericInterface instanceof Class) {
                extractTypeVariablesFromGenericInterfaces(
                        ((Class<?>) genericInterface).getGenericInterfaces(), typeVariableMap);
            }
        }
    }

    private static void populateTypeMapFromParameterizedType(ParameterizedType type, Map<TypeVariable<?>, Type> typeVariableMap) {
        if (type.getRawType() instanceof Class) {
            Type[] actualTypeArguments = type.getActualTypeArguments();
            TypeVariable<?>[] typeVariables = ((GenericDeclaration) type.getRawType()).getTypeParameters();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                TypeVariable<?> variable = typeVariables[i];
                if (actualTypeArgument instanceof Class) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof GenericArrayType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof ParameterizedType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof TypeVariable) {
                    // We have a type that is parameterized at instantiation time
                    // the nearest match on the bridge method will be the bounded type.
                    TypeVariable<?> typeVariableArgument = (TypeVariable<?>) actualTypeArgument;
                    Type resolvedType = typeVariableMap.get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = extractBoundForTypeVariable(typeVariableArgument);
                    }
                    typeVariableMap.put(variable, resolvedType);
                }
            }
        }
    }

}

