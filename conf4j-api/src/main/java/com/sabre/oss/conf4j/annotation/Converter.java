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

package com.sabre.oss.conf4j.annotation;

import com.sabre.oss.conf4j.converter.TypeConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a class that implements {@link TypeConverter} interface. This interface is used to convert a configuration
 * property value to a property type. It is applicable only for value configuration properties
 * (which doesn't return sub-configuration nor a sub-configuration list).
 * <p>
 * {@code TypeConverter} specified by this annotation must provide a public, parameter-less constructor, because
 * it is instantiated on runtime via reflection. It must be <i>thread-safe</i> and <i>stateless</i>.
 * Many instances of the converter are created; therefore, be sure that creating a new instance is fast and the created
 * object doesn't occupy too much memory.
 * </p>
 * <p>
 * <b>Note:</b> Configuration factory is usually pre-configured with a wide range of converters. {@code @Converter}
 * should be used when a type of configuration property is not supported, or there is a need to use a different
 * converter.
 * </p>
 * <b>Example usage:</b>
 * <pre>
 * public interface DateRange {
 *    &#064;Key
 *    &#064;Converter(DateTimeConverter.class)
 *    DateTime getStart();
 *
 *    &#064;Key
 *    &#064;Converter(DateTimeConverter.class)
 *    DateTime getEnd();
 * }
 * </pre>
 *
 * @see TypeConverter
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface Converter {
    /**
     * @return a class which implements {@link TypeConverter} interface which is used for converting
     * configuration property value to a property type.
     */
    Class<? extends TypeConverter<?>> value();
}
