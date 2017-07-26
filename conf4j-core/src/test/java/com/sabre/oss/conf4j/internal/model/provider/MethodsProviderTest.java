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

package com.sabre.oss.conf4j.internal.model.provider;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodsProviderTest {
    MethodsProvider methodsProvider = new MethodsProvider();

    @Test
    public void shouldFindPublicAbstractMethodInClass() throws NoSuchMethodException {
        // given
        abstract class TestClass {
            public abstract int propertyA();
        }

        // when
        Collection<Method> methods = methodsProvider.getAllDeclaredMethods(TestClass.class);

        // then
        assertThat(methods).contains(TestClass.class.getMethod("propertyA"));
    }

    @Test
    public void shouldFindPublicAbstractMethodInheritedFromAbstractMethod() throws NoSuchMethodException {
        // given
        abstract class AbstractClass {
            public abstract int propertyA();
        }

        abstract class TestClass extends AbstractClass {
            public abstract int propertyB();
        }

        // when
        Collection<Method> methods = methodsProvider.getAllDeclaredMethods(TestClass.class);

        // then
        assertThat(methods).contains(AbstractClass.class.getMethod("propertyA"), TestClass.class.getMethod("propertyB"));
    }

    interface TestInterface {
        int propertyA();
    }

    @Test
    public void shouldFindMethodInheritedFromInterface() throws NoSuchMethodException {
        // given
        abstract class TestClass implements TestInterface {
            public abstract int propertyB();
        }

        // when
        Collection<Method> methods = methodsProvider.getAllDeclaredMethods(TestClass.class);

        // then
        assertThat(methods).contains(TestInterface.class.getMethod("propertyA"), TestClass.class.getMethod("propertyB"));
    }

    @Test
    public void shouldHandleMethodFromSubclass() throws NoSuchMethodException {
        // given
        abstract class BaseConf {
            public abstract Integer propertyA();
        }

        abstract class SpecificConf extends BaseConf {
            @Override
            public abstract Integer propertyA();
        }

        abstract class AbstractClass {
            public abstract BaseConf propertyB();
        }

        abstract class TestClass extends AbstractClass {
            @Override
            public abstract SpecificConf propertyB();
        }

        // when
        Collection<Method> methods = methodsProvider.getAllDeclaredMethods(TestClass.class);

        // then
        assertThat(methods).contains(TestClass.class.getMethod("propertyB"));
    }
}
