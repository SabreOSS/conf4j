/*
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

package com.sabre.oss.conf4j.spring.internal.factory.cglib;

import com.sabre.oss.conf4j.internal.factory.SubConfigurationList;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import static com.sabre.oss.conf4j.internal.Constants.COLLECTION_SIZE_SUFFIX;
import static com.sabre.oss.conf4j.internal.utils.PropertyUtils.getPropertyName;

class CglibStaticConfigurationMethodInterceptor extends AbstractCglibConfigurationMethodInterceptor {
    private static final long serialVersionUID = -8710774175759078359L;

    CglibStaticConfigurationMethodInterceptor(ConfigurationModel configurationModel) {
        super(configurationModel);
    }

    @Override
    protected Object interceptInternal(Object obj, Method method, Object[] args, MethodProxy proxy) {
        String propertyName = getPropertyName(method);

        if (subConfigurationProperties.contains(propertyName)) {
            // returns sub-configuration
            return getSubConfigurationProperty(propertyName);
        }

        if (subConfigurationListProperties.contains(propertyName)) {
            // returns sub-configuration list
            Integer size = (Integer) getValueProperty(propertyName + COLLECTION_SIZE_SUFFIX);
            SubConfigurationList list = getSubConfigurationListProperty(propertyName);
            return list.asUnmodifiableList(size);
        }

        // returns value property
        return getValueProperty(propertyName);
    }
}
