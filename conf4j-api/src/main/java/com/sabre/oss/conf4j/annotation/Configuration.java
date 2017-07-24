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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is a markup annotation which indicates an interface or an abstract class is a configuration.
 * <p>
 * <i>Note:</i> Using this annotation is optional. It is required only when the type (or any of the type's method)
 * is not annotated with any annotation from the {@link com.sabre.oss.conf4j.annotation} package.
 * Otherwise the framework recognizes the type as a configuration.
 * </p>
 * <p>
 * Configuration consists of a set of properties. A property can be any type; however, there must be an appropriate
 * {@link TypeConverter} capable of converting type from string. The framework out-of-the-box handles properties
 * of configuration type or a list of configuration types (such configurations are called sub-configurations).
 * Cycles (direct nor indirect) between configurations are not supported.
 * </p>
 * <p>
 * Each configuration property must follow JavaBeans naming conventions,
 * and must be {@code public} and {@code abstract} (which is always true for configurations based on the interface)
 * and optionally annotated with @{@link Key}.
 * </p>
 * <p>
 * <i>Note:</i> {@code @Key} may be required for value configuration property by <i>strict</i>
 * configuration model providers (when default <i>non-strict</i> provider is used).
 * </p>
 * <b>Example configurations:</b>
 * <pre>
 * // Using &#064;Configuration annotation is optional, because the type is annotated
 * // with other conf4j annotations; however, it may be useful for documentation purposes.
 * &#064;Configuration
 * &#064;Key("connection")
 * public interface ConnectionConfiguration {
 *    // &#064;Key is optional here as long as the default, non-strict configuration model provider is used.
 *    &#064;Key
 *    String getUrl();
 *
 *    TimeoutConfiguration getTimeout();
 * }
 * &#064;Configuration
 * public interface TimeoutConfiguration {
 *    &#064;Key("connect")
 *    int getConnectTimeout();
 *
 *    &#064;Key("read")
 *    int getReadTimeout();
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface Configuration {
}
