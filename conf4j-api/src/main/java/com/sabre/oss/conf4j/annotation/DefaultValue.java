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

package com.sabre.oss.conf4j.annotation;

import com.sabre.oss.conf4j.converter.TypeConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the default value for a value property in a configuration class. A value property is defined
 * as a standard Java type or any other type that is not a sub-configuration or a list of sub-configurations.
 * <p>
 * <i>Note:</i> Default value is encoded as a string. It is converted to the value of the property type, which
 * depends on the {@link TypeConverter}. Converters are configurable and it is the developer's responsibility
 * to ensure the converter configuration matches the format of the default value.
 * </p>
 * <b>Example usage:</b>
 * <pre>
 * public interface ConnectionConfiguration {
 *    &#064;Key("name")
 *    &#064;DefaultValue("http://www.sabre.com")
 *    String getUrl();
 *
 *    &#064;Key("timeout")
 *    &#064;DefaultValue("10000")
 *    int getTimeout();
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface DefaultValue {
    /**
     * Used to specify the default value {@code null}. Using {@code NULL} is equivalent to not applying
     * {@code DefaultValue} annotation, but it may be useful to explicitly specify (for documentation purposes)
     * the default value as {@code null}.
     * <p>For types that do not support {@code null}, such as primitives, using {@code NULL} is not supported.</p>
     */
    String NULL = Constants.NULL;

    /**
     * Default value is represented as a string. The format must be compatible with the converter, which is responsible
     * for converting {@code value} to the appropriate type.
     * <p>Use {@link #NULL} to assign {@code null} as a default value.</p>
     *
     * @return default value.
     */
    String value();
}
