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
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.w3c.dom.Element;

import java.lang.reflect.Method;

import static java.lang.Class.forName;
import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static org.springframework.util.StringUtils.hasText;

public class ConverterBeanDefinitionParser extends AbstractBeanDefinitionParser {
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String ORDER_ATTRIBUTE = "order";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        String configurationType = element.getAttribute(CLASS_ATTRIBUTE);
        if (!hasText(configurationType)) {
            parserContext.getReaderContext().error("Attribute 'class' must not be empty", element);
            return null;
        }

        String order = element.getAttribute(ORDER_ATTRIBUTE);
        if (hasText(order)) {
            configurationType = createOrderedProxy(configurationType, valueOf(order));
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(configurationType);
        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        String beanName = resolveId(element, beanDefinition, parserContext);
        registry.registerBeanDefinition(beanName, beanDefinition);

        return beanDefinition;
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        String id = super.resolveId(element, definition, parserContext);
        if (!hasText(id)) {
            id = element.getAttribute(CLASS_ATTRIBUTE);
        }
        return id;
    }

    private String createOrderedProxy(String configurationType, int order) {
        Enhancer enhancer = new Enhancer();
        enhancer.setCallbackFilter(new CallbackFilter() {
            @Override
            public int accept(Method method) {
                return 0;
            }
        });
        //enhancer.setInterfaces(new Class[]{Ordered.class});
        try {
            enhancer.setSuperclass(forName(configurationType));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(format("No %s class", configurationType), e);
        }
        enhancer.setCallback(new OrderingMethodInterceptor(order));
        return enhancer.create().getClass().getName();
    }

    private final class OrderingMethodInterceptor implements MethodInterceptor {
        private static final String GET_ORDER_METHOD = "getOrder";

        private final int order;

        private OrderingMethodInterceptor(int order) {
            this.order = order;
        }

        @Override
        public Object intercept(Object subject, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return isGetOrder(method)
                    ? order
                    : proxy.invoke(subject, args);
        }

        private boolean isGetOrder(Method method) {
            return method.getName().equals(GET_ORDER_METHOD) &&
                    method.getReturnType().equals(int.class) &&
                    method.getParameterCount() == 0;
        }
    }
}
