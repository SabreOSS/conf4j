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

package com.sabre.oss.conf4j.spring.handler;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static com.sabre.oss.conf4j.spring.ConfigurationBeanDefinitionHelper.ConfigurationIndicator.MANUAL;
import static com.sabre.oss.conf4j.spring.ConfigurationBeanDefinitionHelper.addConf4jConfigurationIndicator;

public class ConfigurationBeanDefinitionParser extends AbstractClassBeanDefinitionParser {
    @Override
    protected BeanDefinitionBuilder getBeanDefinitionBuilder(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = super.getBeanDefinitionBuilder(element, parserContext);
        addConf4jConfigurationIndicator(builder.getRawBeanDefinition(), MANUAL);
        return builder;
    }
}
