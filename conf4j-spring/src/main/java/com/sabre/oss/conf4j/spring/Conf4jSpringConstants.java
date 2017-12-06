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

package com.sabre.oss.conf4j.spring;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.internal.model.ConfigurationModelProvider;
import com.sabre.oss.conf4j.source.ConfigurationSource;

import java.util.List;

/**
 * This class defines various constants used in conf4j integration with springframework.
 */
public final class Conf4jSpringConstants {
    /**
     * The name of the bean which converts configuration values encoded as a string to a proper type.
     * It must implement {@link TypeConverter} and is used by {@link ConfigurationFactory}.
     */
    public static final String CONF4J_TYPE_CONVERTER = "com.sabre.oss.conf4j.typeConverter";

    /**
     * The name of the bean which holds the list of {@link com.sabre.oss.conf4j.processor.ConfigurationValueProcessor}.
     * Processors can post-process a value retrieved from the configuration (e.g. by decrypting it). These processors
     * will be applied to the configuration factory via {@link AbstractConfigurationFactory#setConfigurationValueProcessors(List)}.
     */
    public static final String CONF4J_CONFIGURATION_VALUE_PROCESSORS = "com.sabre.oss.conf4j.configurationValueProcessors";

    /**
     * The name of the bean responsible for creating configuration type instances.
     * It must implement {@link ConfigurationFactory} and support the same properties as ({@link AbstractConfigurationFactory}.
     */
    public static final String CONF4J_CONFIGURATION_FACTORY = "com.sabre.oss.conf4j.configurationFactory";

    /**
     * The name of the bean which is used for extracting model from configuration type.
     * It must implement {@link ConfigurationModelProvider} interface.
     */
    public static final String CONF4J_CONFIGURATION_MODEL_PROVIDER = "com.sabre.oss.conf4j.configurationModelProvider";

    /**
     * The name of the bean which resolves configuration keys to values.
     * It must implement {@link ConfigurationModelProvider} interface.
     */
    public static final String CONF4J_CONFIGURATION_SOURCE = "com.sabre.oss.conf4j.configurationSource";

    /**
     * The name of the bean factory post-processor which implements {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}
     * and is responsible for instrumenting configuration bean definitions and binding them with proper
     * instances of {@link ConfigurationFactory}, {@link TypeConverter} and {@link ConfigurationSource}.
     * <p>
     * It is unlikely this bean will be customized. By default it is of {@link ConfigurationBeanFactoryPostProcessor}
     * type.
     * </p>
     */
    public static final String CONF4J_BEAN_FACTORY_POST_PROCESSOR = "com.sabre.oss.conf4j.beanFactoryPostProcessor";

    private Conf4jSpringConstants() {
    }
}
