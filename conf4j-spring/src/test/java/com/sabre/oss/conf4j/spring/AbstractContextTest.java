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

package com.sabre.oss.conf4j.spring;

import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractContextTest {
    @Resource
    protected ApplicationContext applicationContext;

    protected void isRegistered(Class<?> beanClass) {
        Object configuration = applicationContext.getBean(beanClass);
        assertThat(configuration).isNotNull();
    }

    protected void isRegistered(Class<?> beanClass, String expectedName) {
        try {
            applicationContext.getBean(expectedName, beanClass);
        } catch (BeansException e) {
            String[] beanNames = applicationContext.getBeanNamesForType(beanClass);
            if (beanNames.length == 0) {
                fail("Bean of type " + beanClass.getName() + " not found in the context.");
            } else {
                fail("Bean of type " + beanClass.getName() + " with name " + expectedName + " not found in the context, " +
                        "but following bean(s) is/are found " + Arrays.toString(beanNames));
            }
        }
    }

    protected void isNotRegistered(Class<?> beanClass) {
        try {
            applicationContext.getBean(beanClass);
            fail("Bean with class " + beanClass.getName() + " should not be registered in the context.");
        } catch (NoSuchBeanDefinitionException ignore) {
        }
    }
}
