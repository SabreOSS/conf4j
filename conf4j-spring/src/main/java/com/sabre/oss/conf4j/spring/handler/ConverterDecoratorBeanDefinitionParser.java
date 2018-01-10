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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static com.sabre.oss.conf4j.spring.handler.AttributeConstants.CLASS_ATTRIBUTE;
import static com.sabre.oss.conf4j.spring.handler.AttributeConstants.FACTORY_ATTRIBUTE;
import static com.sabre.oss.conf4j.spring.handler.AttributeConstants.ORDER_ATTRIBUTE;
import static java.lang.Integer.valueOf;
import static org.springframework.util.StringUtils.hasText;

public class ConverterDecoratorBeanDefinitionParser extends AbstractClassBeanDefinitionParser {
    private static final String DEFAULT_DECORATING_CONVERTER_FACTORY =
            "com.sabre.oss.conf4j.spring.converter.DefaultDecoratingConverterFactory";
    private static final String CONF4J_DECORATING_CONVERTER_FACTORY_SUFFIX = "$Conf4jDecoratingConverterFactory";
    private static final OrderingProxy proxy = new OrderingProxy();

    @Override
    protected BeanDefinitionBuilder getBeanDefinitionBuilder(Element element, ParserContext parserContext) {
        String beanClassName = element.getAttribute(FACTORY_ATTRIBUTE);
        boolean defaultFactory = false;
        if (!hasText(beanClassName)) {
            beanClassName = element.getAttribute(CLASS_ATTRIBUTE);
            if (!hasText(beanClassName)) {
                parserContext.getReaderContext().error("Attribute 'class' and attribute 'factory' must not be empty", element);
                return null;
            }
            beanClassName = DEFAULT_DECORATING_CONVERTER_FACTORY;
            defaultFactory = true;
        }

        String order = element.getAttribute(ORDER_ATTRIBUTE);
        if (hasText(order)) {
            ClassLoader classLoader = parserContext.getReaderContext().getBeanClassLoader();
            beanClassName = proxy.create(beanClassName, valueOf(order), classLoader);
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClassName);
        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

        if (defaultFactory) {
            builder.addPropertyValue("converterClass", element.getAttribute(CLASS_ATTRIBUTE));
        }

        return builder;
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        String classId = super.resolveId(element, definition, parserContext);
        if (!classId.equals(element.getAttribute(CLASS_ATTRIBUTE))) {
            return classId;
        }
        String factoryId = element.getAttribute(FACTORY_ATTRIBUTE);
        if (!hasText(factoryId)) {
            return classId + CONF4J_DECORATING_CONVERTER_FACTORY_SUFFIX;
        }
        return factoryId;
    }
}
