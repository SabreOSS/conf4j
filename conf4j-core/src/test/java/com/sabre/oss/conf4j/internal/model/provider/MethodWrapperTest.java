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

package com.sabre.oss.conf4j.internal.model.provider;

import com.sabre.oss.conf4j.internal.model.provider.MethodsProvider.MethodWrapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodWrapperTest {
    @Test
    public void shouldCalculateThatMethodsWithSameReturnTypeAreEqual() {
        // given
        class A {
            public int x() {
                return 0;
            }
        }

        class B extends A {
            @Override
            public int x() {
                return 1;
            }
        }

        MethodWrapper m1 = new MethodWrapper(findMethod(A.class, "x"));
        MethodWrapper m2 = new MethodWrapper(findMethod(B.class, "x"));

        // when
        boolean equals = m1.equals(m2);

        // then
        assertThat(equals).isTrue();
    }

    @Test
    public void shouldCalculateThatMethodsWithCovariantReturnTypeAreEqual() {
        // given
        class A {
            public A x() {
                return this;
            }
        }

        class B extends A {
            @Override
            public B x() {
                return this;
            }
        }

        MethodWrapper m1 = new MethodWrapper(findMethod(A.class, "x"));
        MethodWrapper m2 = new MethodWrapper(findMethod(B.class, "x"));

        // when
        boolean equals = m1.equals(m2);

        // then
        assertThat(equals).isTrue();
    }

    @Test
    public void shouldCalculateThatMethodsWithVoidReturnTypeAreEqual() {
        // given
        class A {
            public void x() {
            }
        }

        class B extends A {
            @Override
            public void x() {
            }
        }

        MethodWrapper m1 = new MethodWrapper(findMethod(A.class, "x"));
        MethodWrapper m2 = new MethodWrapper(findMethod(B.class, "x"));

        // when
        boolean equals = m1.equals(m2);

        // then
        assertThat(equals).isTrue();
    }

    @Test
    public void shouldCalculateThatMethodsWithDifferentNamesAreNotEqual() {
        // given
        class A {
            public void a() {
            }
        }

        class B extends A {
            public void b() {
            }
        }

        MethodWrapper m1 = new MethodWrapper(findMethod(A.class, "a"));
        MethodWrapper m2 = new MethodWrapper(findMethod(B.class, "b"));

        // when
        boolean equals = m1.equals(m2);

        // then
        assertThat(equals).isFalse();
    }

    @Test
    public void shouldCalculateThatOverloadedMethodsAreNotEqual() {
        // given
        class A {
            public void x() {
            }
        }

        class B extends A {
            public void x(int x) {
            }
        }

        MethodWrapper m1 = new MethodWrapper(findMethod(A.class, "x"));
        MethodWrapper m2 = new MethodWrapper(findMethod(B.class, "x", int.class));

        // when
        boolean equals = m1.equals(m2);

        // then
        assertThat(equals).isFalse();
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
            for (Method method : methods) {
                if (name.equals(method.getName())
                        && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

}
