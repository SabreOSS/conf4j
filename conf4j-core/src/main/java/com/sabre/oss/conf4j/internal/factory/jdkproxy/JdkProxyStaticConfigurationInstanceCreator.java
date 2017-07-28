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

package com.sabre.oss.conf4j.internal.factory.jdkproxy;

import com.sabre.oss.conf4j.internal.factory.ConfigurationInstanceCreator;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JdkProxyStaticConfigurationInstanceCreator implements ConfigurationInstanceCreator {
    @Override
    public <T> T createInstance(ConfigurationModel configurationModel, ClassLoader classLoader) {
        InvocationHandler invocationHandler = new JdkProxyStaticConfigurationInvocationHandler(configurationModel);
        Class<T> generatedClass = generateClass(configurationModel, classLoader);
        try {
            return generatedClass.getConstructor(InvocationHandler.class).newInstance(invocationHandler);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Class<T> generateClass(ConfigurationModel configurationModel, ClassLoader classLoader) {
        Class<?> configurationType = configurationModel.getConfigurationType();
        @SuppressWarnings("unchecked")
        Class<T> proxyClass = (Class<T>) Proxy.getProxyClass(classLoader, configurationType, Serializable.class);
        return proxyClass;
    }
}
