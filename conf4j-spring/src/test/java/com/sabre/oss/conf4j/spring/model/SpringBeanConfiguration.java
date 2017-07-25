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
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.spring.model.SpringItemConfiguration.SpringItemConfigurationSource;
import com.sabre.oss.conf4j.spring.model.SpringItemConfiguration.SpringItemsConfigurationSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@Component
@Key("springBeanConfigurationPrefix")
public abstract class SpringBeanConfiguration {

    @Key
    @DefaultValue("defaultValue01")
    public abstract String getString01();

    @Key
    @DefaultValue("defaultValue02")
    public abstract String getString02();

    @Key
    @DefaultValue("false")
    public abstract boolean getBooleanPrimitive();

    @Key
    @DefaultValue("false")
    public abstract boolean getDefaultBooleanPrimitive();

    @Key
    @DefaultValue("false")
    public abstract Boolean getBooleanObject();

    @Key
    @DefaultValue("false")
    public abstract Boolean getDefaultBooleanObject();

    @Key("list")
    @DefaultValue("[A,B]")
    public abstract List<String> getListOfStrings();

    @Key("map")
    @DefaultValue("{A:B,C:D}")
    public abstract Map<String, String> getMapOfStringToString();

    @Key
    @DefaultValue("{C:D,E:F,A:B}")
    public abstract SortedMap<String, String> getSortedMap();

    @Key("hidden")
    @DefaultValue("hiddenValue")
    protected abstract String getHiddenProperty();

    public String getComplexProperty() {
        return this.getHiddenProperty();
    }

    @Key("url")
    @DefaultValue("http://127.0.0.1")
    public abstract String getURL();

    @Key("mapWithListAsValue")
    @DefaultValue("{1:[a,b],2:[a,b,c]}")
    public abstract Map<String, List<String>> getComplexListMap();

    @Key("mapWithMapAsValue")
    @DefaultValue("{1:{a:b,c:d},2:{e:f,g:h}}")
    public abstract Map<String, Map<String, String>> getComplexMap();

    @Key("springItems")
    @SpringItemsConfigurationSource(@SpringItemConfigurationSource)
    public abstract List<SpringItemConfiguration> getSpringItemsConfiguration();
}
