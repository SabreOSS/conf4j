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

package com.sabre.oss.conf4j.internal.model.provider.annotation;

import com.sabre.oss.conf4j.annotation.*;
import com.sabre.oss.conf4j.annotation.Meta.Metas;
import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.model.ValuePropertyModel;
import com.sabre.oss.conf4j.internal.model.provider.MetadataExtractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.internal.utils.PropertyUtils.getPropertyName;
import static com.sabre.oss.conf4j.internal.utils.spring.GenericTypeResolver.getTypeVariableMap;
import static com.sabre.oss.conf4j.internal.utils.spring.GenericTypeResolver.resolveReturnType;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;

public class AnnotationValuePropertyMethodParser extends AbstractAnnotationPropertyMethodParser<ValuePropertyModel> {
    private static final List<Class<? extends Annotation>> allowedAnnotations = asList(
            Key.class,
            FallbackKey.class,
            IgnorePrefix.class,
            DefaultValue.class,
            Converter.class,
            Encrypted.class,
            Description.class,
            Meta.class,
            Metas.class
    );

    protected AnnotationValuePropertyMethodParser(MetadataExtractor metadataExtractor) {
        super(metadataExtractor);
    }

    @Override
    public boolean applies(Class<?> configurationType, Method method) {
        return metadataExtractor.isValueConfigurationMethod(configurationType, method);
    }

    @Override
    public ValuePropertyModel process(Class<?> configurationType, Method method) {
        String propertyName = getPropertyName(method);

        Class<?> declaredType = method.getReturnType();
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof TypeVariable) {
            returnType = resolveReturnType(method, configurationType);
        } else if (returnType instanceof ParameterizedType) {
            Type[] types = resolveTypeArguments((ParameterizedType) returnType, getTypeVariableMap(configurationType));
            returnType = parameterize(declaredType, types);
        }

        ValuePropertyModel propertyModel = new ValuePropertyModel(
                propertyName,
                returnType,
                declaredType,
                method,
                metadataExtractor.getDescription(configurationType, method),
                metadataExtractor.getKeys(configurationType, method),
                metadataExtractor.getFallbackKey(configurationType, method),
                metadataExtractor.shouldResetPrefix(configurationType, method),
                metadataExtractor.getDefaultValue(configurationType, method),
                metadataExtractor.getEncryptionProvider(configurationType, method),
                metadataExtractor.getTypeConverter(configurationType, method),
                metadataExtractor.getCustomAttributes(configurationType, method));

        checkPropertyModel(propertyModel);

        return propertyModel;
    }

    private Type[] resolveTypeArguments(ParameterizedType parameterizedType, Map<TypeVariable<?>, Type> typeVariableMap) {
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        return Arrays.stream(typeArguments)
                .map(type -> resolveTypeArgument(type, typeVariableMap))
                .toArray(Type[]::new);
    }

    private Type resolveTypeArgument(Type type, Map<TypeVariable<?>, Type> typeVariableMap) {
        if (type instanceof Class) {
            return type;
        }
        if (type instanceof TypeVariable) {
            return typeVariableMap.get(type);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] types = resolveTypeArguments(parameterizedType, typeVariableMap);
            return parameterize((Class<?>) parameterizedType.getRawType(), types);
        }
        return type;
    }

    private void checkPropertyModel(ValuePropertyModel propertyModel) {
        checkAllowedAnnotations(propertyModel.getMethod(), allowedAnnotations);

        if (propertyModel.getEffectiveKey().isEmpty()) {
            throw new IllegalArgumentException(format("%s hasn't a key assigned, please annotate is with @%s",
                    propertyModel.getMethod(), Key.class.getName()));
        }

        checkTypeConverter(propertyModel);
    }

    private void checkTypeConverter(ValuePropertyModel propertyModel) {
        Type type = propertyModel.getType();
        Class<TypeConverter<?>> typeConverterClass = propertyModel.getTypeConverterClass();
        TypeConverter<?> customTypeConverter = getTypeConverterInstance(typeConverterClass);
        if (customTypeConverter != null) {
            if (!customTypeConverter.isApplicable(type)) {
                throw new IllegalArgumentException(
                        format("%s has custom converter %s which is not compatible with property's type %s.",
                                propertyModel.getMethod(), typeConverterClass.getName(), type.getTypeName()));
            }
        }
    }

    private TypeConverter<?> getTypeConverterInstance(Class<TypeConverter<?>> typeConverterClass) {
        if (typeConverterClass == null) {
            return null;
        }
        try {
            return typeConverterClass.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException("Unable to create type converter instance using parameterless constructor", e);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to create type converter instance, public parameterless constructor not found", e);
        }
    }
}
