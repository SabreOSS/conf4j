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

package com.sabre.oss.conf4j.spring.internal.factory.cglib;

import com.sabre.oss.conf4j.internal.config.DynamicConfiguration;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import org.springframework.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;
import static org.springframework.aop.support.AopUtils.isFinalizeMethod;

class ProxyCallbackFilter implements CallbackFilter {
    static final int CONFIGURATION = 0;
    static final int NO_OVERRIDE = 1;

    private final ConfigurationModel configurationModel;

    ProxyCallbackFilter(ConfigurationModel configurationModel) {
        this.configurationModel = requireNonNull(configurationModel, "configurationModel cannot be null");
    }

    @Override
    public int accept(Method method) {
        return isFinalizeMethod(method) ||
                (!isConfigurationProperty(method) && !isDynamicConfigurationMethod(method))
                ? NO_OVERRIDE : CONFIGURATION;
    }

    private boolean isConfigurationProperty(Method method) {
        return configurationModel.getProperties().stream().anyMatch(p -> p.getMethod().equals(method));
    }

    private boolean isDynamicConfigurationMethod(Method method) {
        return method.getDeclaringClass() == DynamicConfiguration.class;
    }
}
