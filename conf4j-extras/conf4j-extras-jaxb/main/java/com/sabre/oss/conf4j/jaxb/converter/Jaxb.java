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

package com.sabre.oss.conf4j.jaxb.converter;

import com.sabre.oss.conf4j.annotation.Meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.sabre.oss.conf4j.jaxb.converter.Jaxb.CONVERTER;
import static com.sabre.oss.conf4j.jaxb.converter.Jaxb.JAXB;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates the {@link JaxbConverter} should be used for converting a configuration property value to a property type.
 * It applies only when {@link JaxbConverter} is registered in the configuration factory.
 * <p>
 * <b>Example usage:</b>
 * <pre>
 * public interface JaxbConfiguration {
 *     &#064;Jaxb
 *     ComplexType getComplexType();
 * }
 * </pre>
 *
 * @see JaxbConverter
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
@Meta(name = CONVERTER, value = JAXB)
public @interface Jaxb {
    String CONVERTER = "converter";
    String JAXB = "jaxb";
}
