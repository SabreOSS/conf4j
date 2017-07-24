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

import com.sabre.oss.conf4j.internal.config.PropertyMetadata;
import com.sabre.oss.conf4j.internal.factory.ConfigurationPropertiesAccessor;
import com.sabre.oss.conf4j.internal.factory.SubConfigurationList;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.model.PropertyModel;
import com.sabre.oss.conf4j.internal.model.SubConfigurationListPropertyModel;
import com.sabre.oss.conf4j.internal.model.SubConfigurationPropertyModel;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sabre.oss.conf4j.internal.Constants.LIST_SUFFIX;
import static com.sabre.oss.conf4j.internal.Constants.METADATA_SUFFIX;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

abstract class AbstractCglibConfigurationMethodInterceptor implements MethodInterceptor, ConfigurationPropertiesAccessor, Serializable {
    private static final long serialVersionUID = 1;
    /**
     * Holds properties.
     */
    protected final Map<String, Object> properties = new HashMap<>();

    /**
     * Holds property metadata.
     */
    protected final Map<String, PropertyMetadata> propertiesMetadata = new HashMap<>();

    /**
     * Set of sub-configuration properties.
     */
    protected final Set<String> subConfigurationProperties;

    /**
     * Set of sub-configuration list properties.
     */
    protected final Set<String> subConfigurationListProperties;

    AbstractCglibConfigurationMethodInterceptor(ConfigurationModel configurationModel) {
        requireNonNull(configurationModel, "configurationModel cannot be null");

        subConfigurationProperties = configurationModel.getProperties().stream()
                .filter(p -> p instanceof SubConfigurationPropertyModel)
                .map(PropertyModel::getPropertyName)
                .collect(toSet());

        subConfigurationListProperties = configurationModel.getProperties().stream()
                .filter(p -> p instanceof SubConfigurationListPropertyModel)
                .map(PropertyModel::getPropertyName)
                .collect(toSet());
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String methodName = method.getName();
        switch (methodName) {
            case "equals":
                Class<?>[] methodParameterTypes = method.getParameterTypes();
                if (methodParameterTypes.length == 1 && methodParameterTypes[0] == Object.class) {
                    return proxy.invokeSuper(obj, args);
                }
                break;
            case "hashCode":
            case "toString":
                if (method.getParameterTypes().length == 0) {
                    return proxy.invokeSuper(obj, args);
                }
                break;
            default:
                break;
        }

        return interceptInternal(obj, method, args, proxy);
    }

    protected abstract Object interceptInternal(Object obj, Method method, Object[] args, MethodProxy proxy);

    @Override
    public PropertyMetadata getPropertyMetadata(String propertyName) {
        return propertiesMetadata.get(propertyName + METADATA_SUFFIX);
    }

    @Override
    public void setPropertyMetadata(String propertyName, PropertyMetadata propertyMetadata) {
        propertiesMetadata.put(propertyName + METADATA_SUFFIX, propertyMetadata);
    }

    @Override
    public Object getValueProperty(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public void setValueProperty(String propertyName, Object value) {
        properties.put(propertyName, value);
    }

    @Override
    public Object getSubConfigurationProperty(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public void setSubConfigurationProperty(String propertyName, Object subConfiguration) {
        properties.put(propertyName, subConfiguration);
    }

    @Override
    public SubConfigurationList getSubConfigurationListProperty(String propertyName) {
        return (SubConfigurationList) properties.get(propertyName + LIST_SUFFIX);
    }

    @Override
    public void setSubConfigurationListProperty(String propertyName, SubConfigurationList list) {
        properties.put(propertyName + LIST_SUFFIX, list);
    }
}
