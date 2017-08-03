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

import com.sabre.oss.conf4j.annotation.*;
import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.model.provider.MetadataExtractor;
import com.sabre.oss.conf4j.internal.utils.AttributesUtils;
import com.sabre.oss.conf4j.internal.utils.ReflectionUtils;
import com.sabre.oss.conf4j.source.OptionalValue;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import static com.sabre.oss.conf4j.internal.model.provider.annotation.AttributesExtractor.getMetaAttributes;
import static com.sabre.oss.conf4j.internal.utils.CachedAnnotationUtils.*;
import static com.sabre.oss.conf4j.internal.utils.PropertyUtils.getPropertyName;
import static com.sabre.oss.conf4j.internal.utils.spring.GenericTypeResolver.*;
import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.beans.Introspector.decapitalize;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.*;

public class AnnotationMetadataExtractor implements MetadataExtractor {
    private static final Annotation[] EMPTY_ANNOTATIONS = {};

    private static final MetadataExtractor INSTANCE = new AnnotationMetadataExtractor();

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
                isClassAnnotatedByAnyAnnotationInPackage(clazz, Configuration.class.getPackage().getName());
    }

    @Override
    public boolean isAbstractConfiguration(Class<?> clazz) {
        return isAnnotationDeclaredLocally(clazz, AbstractConfiguration.class);
    }

    @Override
    public boolean isValueConfigurationMethod(Class<?> configurationType, Method method) {
        return isConfigurationPropertyMethod(method) &&
                findMethodAnnotation(configurationType, method, Key.class) != null &&
                !isReturnTypeConfiguration(configurationType, method) &&
                !isReturnTypeListOfConfigurations(configurationType, method);
    }

    @Override
    public boolean isSubConfigurationMethod(Class<?> configurationType, Method method) {
        return isConfigurationPropertyMethod(method) &&
                isReturnTypeConfiguration(configurationType, method);
    }

    @Override
    public Class<?> getSubConfigurationType(Class<?> configurationType, Method method) {
        Class<?> subConfigurationClass = getReturnTypeForSubConfiguration(configurationType, method);
        if (!isConfigurationClass(subConfigurationClass)) {
            throw new IllegalArgumentException(format("%s method return type must be sub-configurations, but %s is not.",
                    method, subConfigurationClass.getName()));
        }
        return subConfigurationClass;
    }

    @Override
    public boolean isSubConfigurationListMethod(Class<?> configurationType, Method method) {
        return isConfigurationPropertyMethod(method) &&
                isReturnTypeListOfConfigurations(configurationType, method);
    }

    @Override
    public Class<?> getSubConfigurationListElementType(Class<?> configurationType, Method method) {
        Class<?> subConfigurationClass = getReturnTypeForListOfSubConfigurations(configurationType, method);

        if (!isConfigurationClass(subConfigurationClass)) {
            throw new IllegalArgumentException(format("%s method return type must be list of sub-configurations, but list element %s is not.",
                    method, subConfigurationClass.getName()));
        }

        return subConfigurationClass;
    }

    @Override
    public String getDescription(Class<?> configurationType) {
        Description description = findAnnotation(configurationType, Description.class);
        return description == null ? null : description.value();
    }

    @Override
    public String getDescription(Class<?> configurationType, Method method) {
        Description description = findAnnotation(method, Description.class);
        return description == null ? null : description.value();
    }

    @Override
    public List<String> getKeys(Class<?> configurationType, Method method) {
        Key keyAnnotation = findMethodAnnotation(configurationType, method, Key.class);
        if (keyAnnotation == null) {
            return emptyList();
        }
        if (keyAnnotation.value().length == 0) {
            return singletonList(getPropertyName(method));
        }
        return unmodifiableList(asList(keyAnnotation.value()));
    }

    @Override
    public List<String> getPrefixes(Class<?> configurationType) {
        Key keyAnnotation = findAnnotation(configurationType, Key.class);

        String[] prefixes = (keyAnnotation == null) ? null : keyAnnotation.value();
        if (prefixes != null && prefixes.length == 0) {
            // get default prefix from the class name
            prefixes = new String[]{decapitalize(configurationType.getSimpleName())};
        }

        return prefixes == null ? emptyList() : unmodifiableList(asList(prefixes));
    }

    public List<String> getPrefixes(Class<?> configurationType, Method method) {
        Key keyAnnotation = findAnnotation(method, Key.class);
        IgnoreKey ignoreKeyAnnotation = findAnnotation(method, IgnoreKey.class);
        boolean ignoreKey = ignoreKeyAnnotation != null;
        if (keyAnnotation != null) {
            if (ignoreKey) {
                throw new IllegalArgumentException(
                        format("Mixing %s and %s annotations is not allowed, but method %s breaks this contract.",
                                Key.class.getName(), IgnoreKey.class.getName(), method));
            }

            String[] prefixes = keyAnnotation.value();
            if (prefixes.length == 0) {
                // get default prefix from the property name
                return singletonList(getPropertyName(method));
            } else {
                return unmodifiableList(asList(prefixes));
            }
        } else {
            // For sub-configuration and list of sub-configurations property the key should be extracted from the property
            // name is it was annotated with {@code @Key}.
            if (!isReturnTypeConfiguration(configurationType, method) &&
                    !isReturnTypeListOfConfigurations(configurationType, method)) {
                throw new IllegalArgumentException(format("This method cannot be invoked for value properties but %s is value property method", method));
            }
            return ignoreKey ? emptyList() : singletonList(getPropertyName(method));
        }
    }

    @Override
    public boolean shouldResetPrefix(Class<?> configurationType, Method method) {
        IgnorePrefix ignorePrefix = findAnnotation(method, IgnorePrefix.class);
        return ignorePrefix != null;
    }

    @Override
    public String getFallbackKey(Class<?> configurationType, Method method) {
        FallbackKey fallbackKeyAnnotation = findAnnotation(method, FallbackKey.class);
        return (fallbackKeyAnnotation == null) ? null : fallbackKeyAnnotation.value();
    }

    @Override
    public Class<TypeConverter<?>> getTypeConverter(Class<?> configurationType, Method method) {
        Converter converterAnnotation = findAnnotation(method, Converter.class);
        return converterAnnotation == null ? null : (Class<TypeConverter<?>>) converterAnnotation.value();
    }

    @Override
    public String getEncryptionProvider(Class<?> configurationType, Method method) {
        Encrypted encryptedAnnotation = findAnnotation(method, Encrypted.class);
        return encryptedAnnotation == null ? null : encryptedAnnotation.value();
    }

    @Override
    public Integer getDefaultSubConfigurationListSize(Class<?> configurationType, Method method) {
        DefaultSize defaultSizeAnnotation = findAnnotation(method, DefaultSize.class);
        if (defaultSizeAnnotation == null) {
            return null;
        }
        int defaultSize = defaultSizeAnnotation.value();
        if (defaultSize < 0) {
            throw new IllegalArgumentException(
                    format("Default sub-configuration list cannot be negative, please fix %s annotation on method %s",
                            DefaultSize.class.getName(), method));
        }
        return defaultSize;
    }

    @Override
    public OptionalValue<String> getDefaultValue(Class<?> configurationType, Method method) {
        DefaultValue defaultValueAnnotation = findMethodAnnotation(configurationType, method, DefaultValue.class);
        if (defaultValueAnnotation == null) {
            Class<?> returnType = method.getReturnType();
            return returnType.isPrimitive() ? present(Objects.toString(ReflectionUtils.getDefaultValue(returnType), null)) : absent();
        }

        String value = defaultValueAnnotation.value();
        return present(DefaultValue.NULL.equals(value) ? null : value);
    }

    @Override
    public Map<String, String> getSubConfigurationDefaultValues(Class<?> configurationType, Method method) {
        if (!isAnnotationDeclaredLocally(method.getReturnType(), DefaultsAnnotation.class)) {
            return emptyMap();
        }
        Class<?> defaultsAnnotation = findAnnotation(method.getReturnType(), DefaultsAnnotation.class).value();
        Annotation annotation = findAnnotation(method, (Class<? extends Annotation>) defaultsAnnotation);
        return getDefaultValues(annotation);
    }

    @Override
    public List<Map<String, String>> getSubConfigurationListDefaultValues(Class<?> configurationType, Method method) {
        Class<?> subConfigurationClazz = getSubConfigurationListElementType(configurationType, method);
        Annotation[] defaultsAnnotations = getDefaultsAnnotations(subConfigurationClazz, method);
        return getDefaultValues(defaultsAnnotations);
    }

    @Override
    public Map<String, String> attributes(Class<?> configurationType) {
        return AttributesUtils.attributes(getMetaAttributes(configurationType));
    }

    @Override
    public Map<String, String> attributes(Class<?> configurationType, Method method) {
        return AttributesUtils.attributes(getMetaAttributes(method));
    }

    protected boolean isConfigurationPropertyMethod(Method method) {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        if (void.class.isAssignableFrom(returnType)) {
            return false;
        }
        if (boolean.class.isAssignableFrom(returnType)) {
            // boolean type requires 'is' prefix, but keep it working also for 'get'
            if (!methodName.startsWith("is") && !methodName.startsWith("get")) {
                return false;
            }
        } else {
            if (!methodName.startsWith("get")) {
                return false;
            }
        }

        int modifiers = method.getModifiers();

        return (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) &&
                Modifier.isAbstract(modifiers) &&
                method.getParameters().length == 0;
    }

    protected boolean isReturnTypeConfiguration(Class<?> configurationType, Method method) {
        Class<?> clazz = getReturnTypeForSubConfiguration(configurationType, method);
        return clazz != null && isConfigurationClass(clazz);
    }

    protected boolean isReturnTypeListOfConfigurations(Class<?> configurationType, Method method) {
        Class<?> elementType = getReturnTypeForListOfSubConfigurations(configurationType, method);
        return elementType != null && isConfigurationClass(elementType);
    }

    private Class<?> getReturnTypeForSubConfiguration(Class<?> configurationType, Method method) {
        return resolveReturnType(method, configurationType);
    }

    private Class<?> getReturnTypeForListOfSubConfigurations(Class<?> configurationType, Method method) {
        Class<?> returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        if (List.class.isAssignableFrom(returnType) && genericReturnType instanceof ParameterizedType) {
            Type elementType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
            Class<?> elementClass;
            if (elementType instanceof Class) {
                elementClass = (Class<?>) elementType;
            } else if (elementType instanceof TypeVariable) {
                elementClass = resolveType(elementType, getTypeVariableMap(configurationType));
            } else {
                return null;
            }
            return elementClass;
        } else {
            return null;
        }
    }

    private List<Map<String, String>> getDefaultValues(Annotation[] annotations) {
        if (ArrayUtils.isEmpty(annotations)) {
            return emptyList();
        }
        List<Map<String, String>> result = new ArrayList<>(annotations.length);
        for (Annotation annotation : annotations) {
            result.add(getDefaultValues(annotation));
        }
        return unmodifiableList(result);
    }

    private Map<String, String> getDefaultValues(Annotation annotation) {
        if (annotation == null) {
            return emptyMap();
        }
        Map<String, String> values = new HashMap<>();
        for (Entry<String, Object> entry : getAnnotationAttributes(annotation).entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && !(value instanceof String)) {
                throw new IllegalArgumentException(
                        format("Default value annotation attribute type must be %s but @%s %s attribute's type is %s",
                                String.class.getName(), annotation.getClass().getName(), key, value.getClass().getName())
                );
            }
            if (!DefaultsAnnotation.SKIP.equals(value)) {
                values.put(key, DefaultsAnnotation.NULL.equals(value) ? null : (String) value);
            }
        }
        return unmodifiableMap(values);
    }

    private Annotation[] getDefaultsAnnotations(Class<?> configurationType, Method method) {
        Class<? extends Annotation> targetCollectionDefaultsAnnotationClass = null;

        DefaultsAnnotation defaultsAnnotation = findAnnotation(configurationType, DefaultsAnnotation.class);
        Class<? extends Annotation> defaultsAnnotationTarget = defaultsAnnotation != null ? defaultsAnnotation.value() : null;
        if (defaultsAnnotationTarget != null) {
            Repeatable repeatableAnnotation = findAnnotation(defaultsAnnotationTarget, Repeatable.class);
            if (repeatableAnnotation != null) {
                targetCollectionDefaultsAnnotationClass = repeatableAnnotation.value();
            }
        }

        if (targetCollectionDefaultsAnnotationClass == null) {
            return EMPTY_ANNOTATIONS;
        }

        Annotation defaultsValueAnnotation = findAnnotation(method, targetCollectionDefaultsAnnotationClass);
        if (defaultsValueAnnotation != null) {
            if (getAnnotationAttributes(defaultsValueAnnotation) == null) {
                throw new IllegalArgumentException(targetCollectionDefaultsAnnotationClass.getName() + " requires non null value on method " + method.getName());
            }
        }

        Annotation annotation = findAnnotation(method, targetCollectionDefaultsAnnotationClass);
        if (annotation == null) {
            return EMPTY_ANNOTATIONS;
        }
        Object value = getAnnotationAttributes(annotation).get("value");
        if (value instanceof Annotation[]) {
            return (Annotation[]) value;
        }
        throw new IllegalArgumentException("A 'value' attribute of " + annotation.getClass().getName() + " annotation " +
                "must provide Annotation[], but the type is " + ((value == null) ? "null" : value.getClass()));
    }
}
