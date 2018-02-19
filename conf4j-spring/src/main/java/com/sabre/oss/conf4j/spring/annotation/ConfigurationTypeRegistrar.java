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

package com.sabre.oss.conf4j.spring.annotation;

import com.sabre.oss.conf4j.spring.ConfigurationBeanDefinitionHelper.ConfigurationIndicator;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

import static com.sabre.oss.conf4j.spring.ConfigurationBeanDefinitionHelper.addConf4jConfigurationIndicator;
import static java.util.Arrays.asList;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * {@link ImportBeanDefinitionRegistrar} used by {@link ConfigurationType}.
 */
class ConfigurationTypeRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        List<AnnotationAttributes> configurationTypeAttributesList = new ArrayList<>(1);

        // @ConfigurationTypes
        AnnotationAttributes configurationTypesAttributes = fromMap(importingClassMetadata.getAnnotationAttributes(ConfigurationTypes.class.getName()));
        if (configurationTypesAttributes != null && !configurationTypesAttributes.isEmpty()) {
            configurationTypeAttributesList.addAll(asList(configurationTypesAttributes.getAnnotationArray("value")));
        }

        // @ConfigurationType
        AnnotationAttributes configurationTypeAttributes = fromMap(importingClassMetadata.getAnnotationAttributes(ConfigurationType.class.getName()));
        if (configurationTypeAttributes != null) {
            configurationTypeAttributesList.add(configurationTypeAttributes);
        }

        for (AnnotationAttributes attributes : configurationTypeAttributesList) {
            registerConfigurationType(registry, attributes);
        }
    }

    private void registerConfigurationType(BeanDefinitionRegistry registry, AnnotationAttributes attributes) {
        Class<?> configurationType = attributes.getClass("value");
        String[] names = attributes.getStringArray("name");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(configurationType);
        addConf4jConfigurationIndicator(builder.getRawBeanDefinition(), ConfigurationIndicator.MANUAL);

        String beanName;
        String[] aliases = null;
        if (names.length == 0) {
            beanName = configurationType.getName();
        } else if (names.length == 1) {
            beanName = names[0];
        } else {
            beanName = names[0];
            aliases = ArrayUtils.subarray(names, 1, names.length);
        }

        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
        if (aliases != null) {
            for (String alias : aliases) {
                registry.registerAlias(beanName, alias);
            }
        }
    }
}
