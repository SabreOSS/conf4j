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

package com.sabre.oss.conf4j.spring.converter;

import com.sabre.oss.conf4j.converter.DecoratingConverterFactory;
import com.sabre.oss.conf4j.converter.TypeConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;
import static org.springframework.util.ClassUtils.getConstructorIfAvailable;

public class DefaultDecoratingConverterFactory implements DecoratingConverterFactory {
    private Class<TypeConverter<?>> converterClass;

    public void setConverterClass(Class<TypeConverter<?>> converterClass) {
        this.converterClass = converterClass;
    }

    @Override
    public TypeConverter<?> create(TypeConverter<?> delegate) {
        Constructor<?> constructor = getConstructorIfAvailable(converterClass, TypeConverter.class);
        try {
            if (constructor != null) {
                return (TypeConverter<?>) constructor.newInstance(delegate);
            } else {
                throw new IllegalStateException(format("Class %s doesn't provide a constructor with " +
                        "'com.sabre.oss.conf4j.converter.TypeConverter' parameter type.", converterClass.getName()));
            }
        } catch (InstantiationException e) {
            throw new IllegalStateException(format("Unable to instantiate class %s.", converterClass.getName()), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(format("Inaccessible constructor in %s class.", converterClass.getName()), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(format("Exception during instantiation of %s class.", converterClass.getName()), e);
        }
    }
}
