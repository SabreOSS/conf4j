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

package com.sabre.oss.conf4j.spring.handler;

import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.context.ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * Overrides standard behavior of {@link ComponentScanBeanDefinitionParser} to allow abstract class initialization.
 */
public class ConfigurationScanBeanDefinitionParser extends ComponentScanBeanDefinitionParser {
    private static final String CONFIGURATION_ANNOTATIONS_ATTRIBUTE = "configuration-annotations";

    @Override
    protected ClassPathBeanDefinitionScanner createScanner(XmlReaderContext readerContext, boolean useDefaultFilters) {
        // default filters are removed, to avoid necessity of using @Component and related meta-annotations
        return new ConfigurationClassPathBeanDefinitionScanner(readerContext.getRegistry());
    }

    @Override
    protected ClassPathBeanDefinitionScanner configureScanner(ParserContext parserContext, Element element) {
        ClassPathBeanDefinitionScanner scanner = super.configureScanner(parserContext, element);

        getAnnotationClasses(parserContext, element, scanner.getResourceLoader().getClassLoader())
                .forEach((clazz) -> scanner.addIncludeFilter(new AnnotationTypeFilter(clazz)));

        return scanner;
    }

    private Set<Class<? extends Annotation>> getAnnotationClasses(ParserContext parserContext, Element element, ClassLoader classLoader) {
        Set<Class<? extends Annotation>> configurationAnnotationClasses;
        if (element.hasAttribute(CONFIGURATION_ANNOTATIONS_ATTRIBUTE)) {
            String configurationAnnotations = element.getAttribute(CONFIGURATION_ANNOTATIONS_ATTRIBUTE);
            configurationAnnotations = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(configurationAnnotations);
            configurationAnnotationClasses = stream(tokenizeToStringArray(configurationAnnotations, CONFIG_LOCATION_DELIMITERS))
                    .map(className -> getAnnotationClasses(className, classLoader))
                    .collect(toCollection(LinkedHashSet::new));
        } else {
            configurationAnnotationClasses = singleton(Component.class);
        }
        return configurationAnnotationClasses;
    }

    private Class<? extends Annotation> getAnnotationClasses(String className, ClassLoader classLoader) {
        Class<?> clazz;
        try {
            clazz = ClassUtils.forName(className, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to load class " + className, e);
        }

        if (clazz.isAssignableFrom(Annotation.class)) {
            throw new IllegalArgumentException(clazz + " is not an annotation");
        }

        return (Class<? extends Annotation>) clazz;
    }
}
