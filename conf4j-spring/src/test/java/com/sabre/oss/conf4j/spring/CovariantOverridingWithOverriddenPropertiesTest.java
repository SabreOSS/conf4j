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

package com.sabre.oss.conf4j.spring;

import com.sabre.oss.conf4j.spring.model.inheritance.CommonConfiguration;
import com.sabre.oss.conf4j.spring.model.inheritance.CommonNestedConfiguration;
import com.sabre.oss.conf4j.spring.model.inheritance.SpecificConfiguration;
import com.sabre.oss.conf4j.spring.model.inheritance.SpecificNestedConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:CovariantOverridingWithOverriddenPropertiesTest/bean-overriding.spring.test.xml")
public class CovariantOverridingWithOverriddenPropertiesTest extends AbstractJUnit4SpringContextTests {
    @Autowired
    private SpecificConfiguration specificConfiguration;
    @Autowired
    private CommonConfiguration commonConfiguration;

    @Test
    public void shouldSpecificConfigurationReturnSpecificNestedConfiguration() {
        // when
        SpecificNestedConfiguration specificNested = specificConfiguration.getNested();

        // then
        assertThat(specificNested instanceof SpecificNestedConfiguration).isTrue();
    }

    @Test
    public void shouldCommonConfigurationReturnCommonNestedConfiguration() {
        // when
        CommonNestedConfiguration nested = commonConfiguration.getNested();
        // then
        assertThat(nested instanceof SpecificNestedConfiguration).isFalse();
    }

    @Test
    public void shouldSpecificNestedReturnItsOverriddenValue() {
        // when
        String value = specificConfiguration.getNested().getSpecificProperty();

        // then
        assertThat(value).isEqualTo("2");
    }

    @Test
    public void shouldCommonNestedReturnItsOverriddenValue() {
        // when
        String value = commonConfiguration.getNested().getCommonNestedProperty();

        // then
        assertThat(value).isEqualTo("1");
    }

    @Test
    public void shouldSpecificNestedReturnInheritedOverriddenProperty() {
        // when
        String value = specificConfiguration.getNested().getCommonNestedProperty();

        // then
        assertThat(value).isEqualTo("1");
    }
}
