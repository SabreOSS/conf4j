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

package com.sabre.oss.conf4j.spring.configuration;

import com.sabre.oss.conf4j.spring.annotation.ConfigurationScan;
import com.sabre.oss.conf4j.spring.annotation.ConfigurationType;
import com.sabre.oss.conf4j.spring.annotation.EnableConf4j;
import com.sabre.oss.conf4j.spring.configuration.model.InvalidConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationTypeInvalidConfigurationTest {

    @Test
    public void shouldReportErrorWhenInvalidConfigurationIsRegisteredManually() {
        assertThrows(BeanDefinitionValidationException.class, () -> {
            new AnnotationConfigApplicationContext(RegisterManually.class).close();
        }, "not recognized as configuration type");
    }

    @Configuration
    @EnableConf4j
    @ConfigurationType(value = InvalidConfiguration.class, name = {"name", "alias"})
    public static class RegisterManually {
    }

    @Test
    public void shouldNotReportErrorWhenInvalidConfigurationIsDiscovered() {
        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Discovered.class)) {
            try {
                context.getBean(InvalidConfiguration.class);
            } catch (NoSuchBeanDefinitionException expected) {
                // this is expected - bean definition should be removed
            }
        }
    }

    @Configuration
    @EnableConf4j
    @ConfigurationScan(basePackageClasses = InvalidConfiguration.class)
    public static class Discovered {
    }
}
