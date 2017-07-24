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

package com.sabre.oss.conf4j.spring;

import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import static org.apache.commons.lang3.Validate.validState;

/**
 * Spring factory bean responsible for creating configuration instances with {@link ConfigurationFactory}.
 */
public class ConfigurationFactoryBean implements FactoryBean<Object>, BeanClassLoaderAware {

    private Class<?> configurationType;
    private ConfigurationFactory configurationFactory;
    private ConfigurationValuesSource configurationValuesSource;
    private ClassLoader classLoader;

    /**
     * Configuration type. It must be compatible with {@link ConfigurationFactory} injected via {@link #setConfigurationFactory(ConfigurationFactory)}.
     *
     * @param configurationType configuration type
     */
    @Required
    public void setConfigurationType(Class<?> configurationType) {
        this.configurationType = configurationType;
    }

    /**
     * Specifies configuration factory used for creating a configuration instance.
     *
     * @param configurationFactory configuration factory.
     */
    @Required
    public void setConfigurationFactory(ConfigurationFactory configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    /**
     * Configuration values source.
     *
     * @param configurationValuesSource configuration values source.
     */
    @Required
    public void setConfigurationValuesSource(ConfigurationValuesSource configurationValuesSource) {
        this.configurationValuesSource = configurationValuesSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Creates and initializes configuration instance.
     *
     * @return configuration instance.
     */
    @Override
    public Object getObject() {
        validState(configurationType != null, "configurationType is not set");
        validState(configurationFactory != null, "configurationFactory is not set");
        validState(configurationValuesSource != null, "configurationValuesSource is not set");

        return configurationFactory.createConfiguration(configurationType, configurationValuesSource, classLoader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getObjectType() {
        return configurationType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return true;
    }
}
