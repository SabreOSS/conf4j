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
package com.sabre.oss.conf4j.spring.example;

import com.sabre.oss.conf4j.example.config.connection.Connection;
import com.sabre.oss.conf4j.spring.annotation.ConfigurationScan;
import com.sabre.oss.conf4j.spring.annotation.ConfigurationType;
import com.sabre.oss.conf4j.spring.annotation.EnableConf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;

@EnableConf4j
@PropertySource("classpath:application.properties")
@ConfigurationType(Connection.class)
@ConfigurationScan
public class SpringAnnotationBasedExample {
    @Autowired
    private Connection connection;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringAnnotationBasedExample.class)) {
            context.getBean(SpringAnnotationBasedExample.class).run();
        }
    }

    private void run() {
        System.out.println("Connection configuration");
        System.out.println("url:              " + connection.getUrl());
        System.out.println("login:            " + connection.getLogin());
        System.out.println("password:         " + connection.getPassword());
        System.out.println("compression:      " + connection.isCompressionEnabled());
        System.out.println("connect timeout:  " + connection.getTimeout().getConnectTimeout());
        System.out.println("read timeout:     " + connection.getTimeout().getReadTimeout());
        System.out.println("max retries:      " + connection.getRetry().getMaxRetries());
        System.out.println("retry delays:     " + connection.getRetry().getDelays());
        System.out.println("properties:       " + connection.getProperties());
        System.out.println();

        System.out.println("Application configuration");
        System.out.println("name: " + applicationConfiguration.getName());
        System.out.println();
    }
}
