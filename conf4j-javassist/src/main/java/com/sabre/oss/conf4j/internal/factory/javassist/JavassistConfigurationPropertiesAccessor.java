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

package com.sabre.oss.conf4j.internal.factory.javassist;

import com.sabre.oss.conf4j.internal.config.PropertyMetadata;
import com.sabre.oss.conf4j.internal.factory.ConfigurationPropertiesAccessor;
import com.sabre.oss.conf4j.internal.factory.SubConfigurationList;
import com.sabre.oss.conf4j.internal.utils.PropertyUtils;

import static com.sabre.oss.conf4j.internal.Constants.LIST_SUFFIX;
import static com.sabre.oss.conf4j.internal.Constants.METADATA_SUFFIX;
import static java.util.Objects.requireNonNull;

class JavassistConfigurationPropertiesAccessor implements ConfigurationPropertiesAccessor {
    private final Object configuration;

    JavassistConfigurationPropertiesAccessor(Object configuration) {
        this.configuration = requireNonNull(configuration, "configuration cannot be null");
    }

    @Override
    public PropertyMetadata getPropertyMetadata(String propertyName) {
        return (PropertyMetadata) getProperty(propertyName + METADATA_SUFFIX);
    }

    @Override
    public void setPropertyMetadata(String propertyName, PropertyMetadata propertyMetadata) {
        setProperty(propertyName + METADATA_SUFFIX, propertyMetadata);
    }

    @Override
    public Object getValueProperty(String propertyName) {
        return getProperty(propertyName);
    }

    @Override
    public void setValueProperty(String propertyName, Object value) {
        setProperty(propertyName, value);
    }

    @Override
    public Object getSubConfigurationProperty(String propertyName) {
        return getProperty(propertyName);
    }

    @Override
    public void setSubConfigurationProperty(String propertyName, Object subConfiguration) {
        setProperty(propertyName, subConfiguration);
    }

    @Override
    public SubConfigurationList getSubConfigurationListProperty(String propertyName) {
        return (SubConfigurationList) getProperty(propertyName + LIST_SUFFIX);
    }

    @Override
    public void setSubConfigurationListProperty(String propertyName, SubConfigurationList list) {
        setProperty(propertyName + LIST_SUFFIX, list);
    }

    private Object getProperty(String propertyName) {
        return PropertyUtils.getProperty(configuration, propertyName);
    }

    private void setProperty(String propertyName, Object value) {
        PropertyUtils.setProperty(configuration, propertyName, value);
    }
}
