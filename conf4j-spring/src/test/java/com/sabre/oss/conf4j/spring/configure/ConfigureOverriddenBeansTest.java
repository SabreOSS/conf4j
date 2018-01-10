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

package com.sabre.oss.conf4j.spring.configure;

import com.sabre.oss.conf4j.converter.StringConverter;
import com.sabre.oss.conf4j.factory.jdkproxy.JdkProxyDynamicConfigurationFactory;
import com.sabre.oss.conf4j.source.MapConfigurationSource;
import com.sabre.oss.conf4j.spring.AbstractContextTest;
import com.sabre.oss.conf4j.spring.ConfigurationBeanFactoryPostProcessor;
import com.sabre.oss.conf4j.spring.configscan.model.RootConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;

import static com.sabre.oss.conf4j.spring.Conf4jSpringConstants.*;

@ContextConfiguration(classes = ConfigureOverriddenBeansTest.class)
@ImportResource("classpath:configure/configure-overridden-beans.spring.test.xml")
public class ConfigureOverriddenBeansTest extends AbstractContextTest {
    @Autowired
    protected RootConfiguration rootConfiguration;

    @Test
    public void shouldRegisterInfrastructureBeans() {
        isRegistered(JdkProxyDynamicConfigurationFactory.class, CONF4J_CONFIGURATION_FACTORY);
        isRegistered(StringConverter.class, CONF4J_TYPE_CONVERTER);
        isRegistered(MapConfigurationSource.class, CONF4J_CONFIGURATION_SOURCE);
        isRegistered(ConfigurationBeanFactoryPostProcessor.class, CONF4J_BEAN_FACTORY_POST_PROCESSOR);
    }
}

