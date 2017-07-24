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
import com.sabre.oss.conf4j.internal.model.ConfigurationModelProvider;
import com.sabre.oss.conf4j.internal.model.SubConfigurationListPropertyModel;
import com.sabre.oss.conf4j.internal.model.provider.MetadataExtractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.internal.utils.PropertyUtils.getPropertyName;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class AnnotationSubConfigurationListPropertyMethodParser extends AbstractAnnotationPropertyMethodParser<SubConfigurationListPropertyModel> {
    private static final List<Class<? extends Annotation>> allowedAnnotations = asList(
            DefaultsAnnotation.class,
            DefaultSize.class,
            Key.class,
            FallbackKey.class,
            IgnorePrefix.class,
            IgnoreKey.class,
            Description.class,
            Meta.class,
            Metas.class
    );

    private final ConfigurationModelProvider provider;

    protected AnnotationSubConfigurationListPropertyMethodParser(MetadataExtractor metadataExtractor, ConfigurationModelProvider provider) {
        super(metadataExtractor);
        this.provider = requireNonNull(provider, "provider cannot be null");
    }

    @Override
    public boolean applies(Class<?> configurationType, Method method) {
        return metadataExtractor.isSubConfigurationListMethod(configurationType, method);
    }

    @Override
    public SubConfigurationListPropertyModel process(Class<?> configurationType, Method method) {
        Class<?> elementType = metadataExtractor.getSubConfigurationListElementType(configurationType, method);
        List<Map<String, String>> defaultValues = metadataExtractor.getSubConfigurationListDefaultValues(configurationType, method);
        int defaultSize = defaultIfNull(metadataExtractor.getDefaultSubConfigurationListSize(configurationType, method), defaultValues.size());

        SubConfigurationListPropertyModel propertyModel = new SubConfigurationListPropertyModel(
                getPropertyName(method),
                elementType,
                method,
                provider.getConfigurationModel(elementType),
                metadataExtractor.getDescription(configurationType, method),
                metadataExtractor.getPrefixes(configurationType, method),
                metadataExtractor.shouldResetPrefix(configurationType, method),
                defaultSize,
                defaultValues,
                metadataExtractor.getCustomAttributes(configurationType, method));

        checkPropertyModel(propertyModel);

        return propertyModel;
    }

    private void checkPropertyModel(SubConfigurationListPropertyModel propertyModel) {
        checkAllowedAnnotations(propertyModel.getMethod(), allowedAnnotations);
    }

}
