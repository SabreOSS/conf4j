/*
 * MIT License
 *
 * Copyright 2017-2018 Sabre GLBL Inc.
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

package com.sabre.oss.conf4j.factory.model.collections;

import com.sabre.oss.conf4j.annotation.Default;
import com.sabre.oss.conf4j.annotation.DefaultsAnnotation;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.factory.model.collections.Component.ComponentAnnotationSource;
import com.sabre.oss.conf4j.factory.model.collections.SubComponent.SubComponentConfigurationSource;
import com.sabre.oss.conf4j.factory.model.collections.SubComponent.SubComponentsConfigurationSource;

import java.lang.annotation.*;
import java.util.List;

import static com.sabre.oss.conf4j.annotation.Default.NULL;

@Key
@DefaultsAnnotation(ComponentAnnotationSource.class)
public interface Component {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(ComponentAnnotationSources.class)
    @interface ComponentAnnotationSource {
        String name() default NULL;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ComponentAnnotationSources {
        ComponentAnnotationSource[] value();
    }

    @Key
    @Default("defaultName")
    String getName();

    @Key
    String getDescription();

    @Key("subComponent")
    @SubComponentsConfigurationSource({
            @SubComponentConfigurationSource(propertyA = "A0", propertyB = "B0"),
            @SubComponentConfigurationSource(propertyB = "B1")})
    List<SubComponent> getSubComponents();
}
