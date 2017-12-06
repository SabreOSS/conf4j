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

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.factory.javassist.JavassistDynamicConfigurationFactory;
import com.sabre.oss.conf4j.internal.model.ConfigurationModelProvider;
import com.sabre.oss.conf4j.spring.AbstractContextTest;
import com.sabre.oss.conf4j.spring.ConfigurationBeanFactoryPostProcessor;
import com.sabre.oss.conf4j.spring.configscan.model.ConfigurationWithName;
import com.sabre.oss.conf4j.spring.configscan.model.RootConfiguration;
import com.sabre.oss.conf4j.spring.configscan.model.SubConfiguration;
import com.sabre.oss.conf4j.spring.source.PropertySourceConfigurationSource;
import org.junit.Test;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

import static com.sabre.oss.conf4j.spring.Conf4jSpringConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = ConfigureTest.class)
@ImportResource("classpath:configure/configure.spring.test.xml")
public class ConfigureTest extends AbstractContextTest {
    @Resource
    protected RootConfiguration rootConfiguration;

    @Test
    public void shouldRegisterInfrastructureBeans() {
        isRegistered(JavassistDynamicConfigurationFactory.class, CONF4J_CONFIGURATION_FACTORY);
        isRegistered(TypeConverter.class, CONF4J_TYPE_CONVERTER);
        isRegistered(PropertySourceConfigurationSource.class, CONF4J_CONFIGURATION_SOURCE);
        isRegistered(ConfigurationModelProvider.class, CONF4J_CONFIGURATION_MODEL_PROVIDER);
        isRegistered(ConfigurationBeanFactoryPostProcessor.class, CONF4J_BEAN_FACTORY_POST_PROCESSOR);
    }

    @Test
    public void shouldRegisterConfigurations() {
        isRegistered(RootConfiguration.class);
        isRegistered(ConfigurationWithName.class, "customPrefix.configurationWithName");
        isNotRegistered(SubConfiguration.class);
        assertThat(rootConfiguration.getName()).isEqualTo("defaultName");
    }


}
