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

import org.springframework.cglib.proxy.*;
import org.springframework.core.Ordered;

import static java.lang.Class.forName;
import static java.lang.String.format;
import static org.springframework.cglib.proxy.Enhancer.registerStaticCallbacks;

public class OrderingProxy {
    private static final String GET_ORDER_METHOD = "getOrder";

    public String create(String configurationType, int order) {
        Enhancer enhancer = new Enhancer();
        enhancer.setCallbackFilter(getGetOrderMethodFilter());
        enhancer.setCallbackTypes(new Class[]{OrderingCallBack.class, NoOp.class});
        enhancer.setInterfaces(new Class[]{Ordered.class});
        // TODO Proper String->Class resolving
        try {
            enhancer.setSuperclass(forName(configurationType));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(format("No %s class.", configurationType), e);
        }
        Class<?> enhancerClass = enhancer.createClass();
        registerStaticCallbacks(enhancerClass, new Callback[]{new OrderingCallBack(order), NoOp.INSTANCE});
        return enhancerClass.getName();
    }

    private CallbackFilter getGetOrderMethodFilter() {
        return m -> GET_ORDER_METHOD.equals(m.getName()) &&
                int.class.equals(m.getReturnType()) &&
                m.getParameterCount() == 0 ? 0 : 1;
    }

    private final class OrderingCallBack implements FixedValue {
        private final int order;

        private OrderingCallBack(int order) {
            this.order = order;
        }

        @Override
        public Object loadObject() throws Exception {
            return order;
        }
    }
}
