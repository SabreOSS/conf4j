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

package com.sabre.oss.conf4j.internal.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static com.sabre.oss.conf4j.internal.utils.PropertyUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PropertyUtilsTest {
    @Test
    public void shouldGetProperties() {
        Bean bean = new Bean();

        assertThat(getProperty(bean, "intValue")).isEqualTo(0);
        assertThat(getProperty(bean, "stringValue")).isNull();
        assertThat(getProperty(bean, "beanValue")).isNull();

        bean.setIntValue(1);
        assertThat(getProperty(bean, "intValue")).isEqualTo(bean.getIntValue());

        bean.setStringValue("string");
        assertThat(getProperty(bean, "stringValue")).isSameAs(bean.getStringValue());

        bean.setBeanValue(new Bean());
        assertThat(getProperty(bean, "beanValue")).isSameAs(bean.getBeanValue());
    }

    @Test
    public void shouldSetProperties() {
        Bean bean = new Bean();

        setProperty(bean, "intValue", 1);
        assertThat(bean.getIntValue()).isEqualTo(1);

        setProperty(bean, "stringValue", "string");
        assertThat(bean.getStringValue()).isEqualTo("string");

        Bean beanValue = new Bean();
        setProperty(bean, "beanValue", beanValue);
        assertThat(bean.getBeanValue()).isSameAs(beanValue);

        // check if null is set properly
        setProperty(bean, "stringValue", null);
        assertThat(bean.getStringValue()).isNull();
    }

    @Test
    public void getPropertyShouldThrowNoSuchMethodExceptionWhenPropertyNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            getProperty(new Bean(), "unknownProperty");
        });
    }

    @Test
    public void getPropertyShouldThrowNoSuchMethodExceptionWhenPropertyIsCapitalized() {
        assertThrows(IllegalArgumentException.class, () -> {
            getProperty(new Bean(), "BeanValue");
        });
    }

    @Test
    public void getPropertyShouldThrowNullPointerExceptionWhenNullBean() {
        assertThrows(NullPointerException.class, () -> {
            getProperty(null, "intValue");
        });
    }

    @Test
    public void getPropertyShouldThrowNullPointerExceptionWhenNullPropertyName() {
        assertThrows(NullPointerException.class, () -> {
            getProperty(new Bean(), null);
        });
    }

    @Test
    public void setPropertyShouldThrowExceptionWhenPropertyTypeMismatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            setProperty(new Bean(), "beanValue", "invalid-type");
        });
    }

    @Test
    public void setPropertyShouldThrowNoSuchMethodExceptionWhenPropertyNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            setProperty(new Bean(), "unknownProperty", null);
        });
    }

    @Test
    public void setPropertyShouldThrowNoSuchMethodExceptionWhenPropertyIsCapitalized() {
        assertThrows(IllegalArgumentException.class, () -> {
            setProperty(new Bean(), "BeanValue", null);
        });
    }

    @Test
    public void setPropertyShouldThrowNullPointerExceptionWhenNullBean() {
        assertThrows(NullPointerException.class, () -> {
            setProperty(null, "intValue", 1);
        });
    }

    @Test
    public void setPropertyShouldThrowNullPointerExceptionWhenNullPropertyName() {
        assertThrows(NullPointerException.class, () -> {
            setProperty(new Bean(), null, null);
        });
    }

    @Test
    public void getPropertyNameShouldProvidePropertyNameFromValidAccessor() {
        assertThat(getPropertyName(getMethod(Bean.class, "getIntValue"))).isEqualTo("intValue");
        assertThat(getPropertyName(getMethod(Bean.class, "setIntValue"))).isEqualTo("intValue");
        assertThat(getPropertyName(getMethod(Bean.class, "getStringValue"))).isEqualTo("stringValue");
        assertThat(getPropertyName(getMethod(Bean.class, "isBooleanValue"))).isEqualTo("booleanValue");
        assertThat(getPropertyName(getMethod(Bean.class, "setBooleanValue"))).isEqualTo("booleanValue");
    }

    @Test
    public void getPropertyNameShouldThrowNPEWhenMethodIsNull() {
        assertThrows(NullPointerException.class, () -> {
            getPropertyName(null);
        });
    }

    @Test
    public void getPropertyNameShouldThrowIAEWhenMethodIsNotGetterNorSetter() {
        assertThrows(IllegalArgumentException.class, () -> {
            getPropertyName(getMethod(Bean.class, "equals"));
        });
    }

    private static Method getMethod(Class<?> clazz, String methodName) {
        return Arrays.stream(clazz.getMethods())
                .filter(m -> methodName.equals(m.getName()))
                .findAny()
                .get();
    }

    private static class Bean {
        private int intValue;
        private String stringValue;
        private Bean beanValue;
        private boolean booleanValue;

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }

        public Bean getBeanValue() {
            return beanValue;
        }

        public void setBeanValue(Bean beanValue) {
            this.beanValue = beanValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
    }
}
