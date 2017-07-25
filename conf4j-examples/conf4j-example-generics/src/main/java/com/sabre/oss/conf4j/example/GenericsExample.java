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

package com.sabre.oss.conf4j.example;

import com.sabre.oss.conf4j.example.config.*;
import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.factory.jdkproxy.JdkProxyStaticConfigurationFactory;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;
import com.sabre.oss.conf4j.source.LoggingConfigurationValuesSource;
import com.sabre.oss.conf4j.source.PropertiesConfigurationValuesSource;

/**
 * Example application which demonstrates how to use generics in the configuration types.
 * <p>
 * {@link ValidatorConfiguration} is an abstract configuration type
 * which defines basic properties like {@link ValidatorConfiguration#isEnabled() enabled} as well as generic
 * {@link ValidatorConfiguration#getConstraints() constraints}. There are two concrete configuration types:
 * {@link IntegerValidatorConfiguration} and {@link StringValidatorConfiguration} which defines the appropriate
 * constraints, respectively {@link IntegerConstraints} and {@link StringConstraints}. Both constraints extends the
 * {@link MinMaxConstraints} interface, which provides common properties
 * {@link MinMaxConstraints#getMin() min} and {@link MinMaxConstraints#getMax() max}.
 * </p>
 * Because {@link MinMaxConstraints} is also parametrized, the actual type of {@code min} and {@code max} properties
 * is defined by concrete constraint e.g. <pre>{@code public interface IntegerConstraints extends MinMaxConstraint<Integer> {...}}</pre>
 */
public class GenericsExample {
    // Creates a configuration factory. It is usually shared across the entire application.
    private final ConfigurationFactory factory = new JdkProxyStaticConfigurationFactory();

    public static void main(String[] args) {
        new GenericsExample().run();
    }

    private void run() {
        // Creates a configuration values source from the property files.
        ConfigurationValuesSource source = new PropertiesConfigurationValuesSource(getClass().getResource("/application.properties").getFile());

        source = new LoggingConfigurationValuesSource(source);
        // Creates the validators configuration instance and binds them to the values source.
        IntegerValidatorConfiguration integerValidator = factory.createConfiguration(IntegerValidatorConfiguration.class, source);
        StringValidatorConfiguration stringValidator = factory.createConfiguration(StringValidatorConfiguration.class, source);

        // Now you can access configuration properties.

        System.out.println("Integer validator configuration:");
        System.out.println("name:       " + integerValidator.getName());
        System.out.println("enabled:    " + integerValidator.isEnabled());
        System.out.println("min:        " + integerValidator.getConstraints().getMin());
        System.out.println("max:        " + integerValidator.getConstraints().getMax());
        System.out.println();

        System.out.println("String validator configuration:");
        System.out.println("name:       " + stringValidator.getName());
        System.out.println("enabled:    " + stringValidator.isEnabled());
        System.out.println("min:        " + stringValidator.getConstraints().getMin());
        System.out.println("max:        " + stringValidator.getConstraints().getMax());
        System.out.println("min length: " + stringValidator.getConstraints().getMinLength());
        System.out.println("max length: " + stringValidator.getConstraints().getMaxLength());
        System.out.println("pattern     " + stringValidator.getConstraints().getPattern());
        System.out.println();
    }
}
