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

package com.sabre.oss.conf4j.example;

import com.sabre.oss.conf4j.annotation.Meta;
import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.factory.jdkproxy.JdkProxyStaticConfigurationFactory;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;

/**
 * Example application which demonstrates how to use <i>custom meta-data attributes</i> defined via {@link Meta} annotation.
 */
public class MetaDataExample {
    // Create a configuration factory. It is usually shared across entire application.
    private final ConfigurationFactory factory = new JdkProxyStaticConfigurationFactory();

    public static void main(String[] args) {
        new MetaDataExample().run();
    }

    private void run() {
        // Create a configuration values source which is aware of custom meta-data.
        ConfigurationValuesSource source = new SourceFileAwareConfigurationValuesSource();

        // Create a configuration instance and bind it to the values source.
        Connection connection = factory.createConfiguration(Connection.class, source);

        // Now you can access configuration properties.
        // All values except for 'password' are from application.properties file.
        System.out.println("url:              " + connection.getUrl());
        System.out.println("login:            " + connection.getLogin());
        // 'password' property is from secrets.properties
        System.out.println("password:         " + connection.getPassword());
    }
}
