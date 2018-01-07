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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class AbstractClassBeanDefinitionParser extends AbstractBeanDefinitionParser {
    private static final String CLASS_ATTRIBUTE = "class";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        String configurationType = element.getAttribute(CLASS_ATTRIBUTE);
        if (!StringUtils.hasText(configurationType)) {
            parserContext.getReaderContext().error("Attribute 'class' must not be empty", element);
            return null;
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(configurationType);
        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

        addMetadata(builder);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        String beanName = resolveId(element, beanDefinition, parserContext);
        registry.registerBeanDefinition(beanName, beanDefinition);

        return beanDefinition;
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        String id = super.resolveId(element, definition, parserContext);
        if (!StringUtils.hasText(id)) {
            id = element.getAttribute(CLASS_ATTRIBUTE);
        }
        return id;
    }

    protected void addMetadata(BeanDefinitionBuilder builder) {
    }
}
