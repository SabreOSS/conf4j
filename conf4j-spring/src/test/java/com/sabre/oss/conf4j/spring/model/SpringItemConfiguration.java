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

package com.sabre.oss.conf4j.spring.model;

import com.sabre.oss.conf4j.annotation.DefaultValue;
import com.sabre.oss.conf4j.annotation.DefaultsAnnotation;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.spring.model.SpringItemConfiguration.SpringItemConfigurationSource;

import java.lang.annotation.*;

@Key("springItemConfigurationPrefix")
@DefaultsAnnotation(SpringItemConfigurationSource.class)
public interface SpringItemConfiguration {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(SpringItemsConfigurationSource.class)
    @Documented
    @interface SpringItemConfigurationSource {
        String property1() default DefaultsAnnotation.NULL;

        String property2() default DefaultsAnnotation.SKIP;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Documented
    @interface SpringItemsConfigurationSource {
        SpringItemConfigurationSource[] value();
    }

    @Key
    @DefaultValue("property1DefaultValue")
    String getProperty1();

    @Key
    @DefaultValue("property2DefaultValue")
    String getProperty2();
}
