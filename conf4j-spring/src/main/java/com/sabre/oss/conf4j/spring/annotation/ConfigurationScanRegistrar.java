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

package com.sabre.oss.conf4j.spring.annotation;

import com.sabre.oss.conf4j.spring.handler.ConfigurationClassPathBeanDefinitionScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.util.ClassUtils.getPackageName;

/**
 * {@link ImportBeanDefinitionRegistrar} used by {@link ConfigurationScan}.
 */
class ConfigurationScanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {
    private Environment environment;
    private ResourceLoader resourceLoader;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);
        Set<Class<? extends Annotation>> configurationAnnotationClasses = getConfigurationAnnotationClasses(importingClassMetadata);

        ConfigurationClassPathBeanDefinitionScanner scanner = new ConfigurationClassPathBeanDefinitionScanner(registry);
        scanner.setEnvironment(environment);
        scanner.setResourceLoader(resourceLoader);
        for (Class<? extends Annotation> configurationAnnotationClass : configurationAnnotationClasses) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(configurationAnnotationClass));
        }
        scanner.scan(packagesToScan.toArray(toArray(packagesToScan)));
    }

    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(ConfigurationScan.class.getName()));
        String[] value = attributes.getStringArray("value");
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");

        if (!ObjectUtils.isEmpty(value)) {
            Assert.state(ObjectUtils.isEmpty(basePackages),
                    "@ConfigurationScan basePackages and value attributes are mutually exclusive");
        }

        Set<String> packagesToScan = new LinkedHashSet<>();
        packagesToScan.addAll(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(getPackageName(basePackageClass));
        }

        return packagesToScan.isEmpty() ? singleton(getPackageName(metadata.getClassName())) : packagesToScan;
    }

    private Set<Class<? extends Annotation>> getConfigurationAnnotationClasses(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(ConfigurationScan.class.getName()));
        Class<?>[] configurationAnnotations = attributes.getClassArray("configurationAnnotations");
        return Arrays.stream(configurationAnnotations)
                .map(a -> (Class<? extends Annotation>) a)
                .collect(toCollection(LinkedHashSet::new));
    }

    private String[] toArray(Set<String> set) {
        return set.toArray(new String[set.size()]);
    }

}
