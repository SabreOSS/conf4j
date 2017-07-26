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

package com.sabre.oss.conf4j.internal.model.provider;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.source.Attributes;
import com.sabre.oss.conf4j.source.OptionalValue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface MetadataExtractor {
    boolean isConfigurationClass(Class<?> clazz);

    boolean isAbstractConfiguration(Class<?> clazz);

    boolean isValueConfigurationMethod(Class<?> configurationType, Method method);

    boolean isSubConfigurationMethod(Class<?> configurationType, Method method);

    Class<?> getSubConfigurationType(Class<?> configurationType, Method method);

    boolean isSubConfigurationListMethod(Class<?> configurationType, Method method);

    Class<?> getSubConfigurationListElementType(Class<?> configurationType, Method method);

    String getDescription(Class<?> configurationType);

    String getDescription(Class<?> configurationType, Method method);

    List<String> getKeys(Class<?> configurationType, Method method);

    List<String> getPrefixes(Class<?> configurationType);

    List<String> getPrefixes(Class<?> configurationType, Method method);

    boolean shouldResetPrefix(Class<?> configurationType, Method method);

    String getFallbackKey(Class<?> configurationType, Method method);

    Class<TypeConverter<?>> getTypeConverter(Class<?> configurationType, Method method);

    String getEncryptionProvider(Class<?> configurationType, Method method);

    Integer getDefaultSubConfigurationListSize(Class<?> configurationType, Method method);

    OptionalValue<String> getDefaultValue(Class<?> configurationType, Method method);

    Map<String, String> getSubConfigurationDefaultValues(Class<?> configurationType, Method method);

    List<Map<String, String>> getSubConfigurationListDefaultValues(Class<?> configurationType, Method method);

    Attributes getCustomAttributes(Class<?> configurationType);

    Attributes getCustomAttributes(Class<?> configurationType, Method method);
}
