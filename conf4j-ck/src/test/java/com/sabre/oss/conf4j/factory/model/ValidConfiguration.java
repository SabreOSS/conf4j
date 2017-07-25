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

package com.sabre.oss.conf4j.factory.model;

import com.sabre.oss.conf4j.annotation.DefaultValue;
import com.sabre.oss.conf4j.annotation.FallbackKey;
import com.sabre.oss.conf4j.annotation.Key;

import java.util.List;

@Key("configuration.under.test")
public interface ValidConfiguration {
    @Key("boolean.property")
    @DefaultValue("true")
    boolean isSimpleBooleanProperty();

    @Key("Boolean.property")
    @DefaultValue("true")
    Boolean getBooleanProperty();

    @Key("int.property")
    @DefaultValue("1")
    int getSimpleIntegerProperty();

    @Key("Integer.property")
    @DefaultValue("-1")
    Integer getIntegerProperty();

    @Key("long.property")
    @DefaultValue("1")
    long getSimpleLongProperty();

    @Key("Long.property")
    @DefaultValue("-1")
    Long getLongProperty();

    @Key("double.property")
    @DefaultValue("0.1")
    double getSimpleDoubleProperty();

    @Key("Double.property")
    @DefaultValue("-0.1")
    Double getDoubleProperty();

    @Key("String.property")
    @DefaultValue("defaultValue")
    String getStringProperty();

    @Key("List.property")
    @DefaultValue("[defaultValue1,defaultValue2]")
    List<String> getStringListProperty();

    @Key("enumList.property")
    @DefaultValue("[VALUE_1,VALUE_2]")
    List<SimpleEnum> getEnumListProperty();

    enum SimpleEnum {
        VALUE_1, VALUE_2
    }

    @Key("property")
    @FallbackKey("fallback.property")
    @DefaultValue("defaultValue")
    String getGlobalFallbackProperty();

    @Key("List.defaultEmpty")
    @DefaultValue("[]")
    List<String> getStringListPropertyWithEmptyDefault();

    @Key("List.withoutDefault")
    List<String> getStringListPropertyWithoutDefault();

    @Key("List.defaultNull")
    @DefaultValue(DefaultValue.NULL)
    List<String> getStringListPropertyWithNullDefault();
}
