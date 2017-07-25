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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation allows you to ignore a key associated with the configuration property in which the return type
 * is a sub-configuration or a list of sub-configurations.
 * For such properties, even if it is not explicitly annotated with {@link Key}, the property name is appended
 * to the key prefix. {@code @IgnoreKey} is used when there is a need to remove the property name from the middle
 * of a key.
 * <p>
 * If the property is annotated with {@link Key @Key}, {@code @IgnoreKey} should not be used.
 * {@code @Key} explicitly associates the key with the property,
 * but {@code @IgnoreKey} indicates such key should be ignored.
 * </p>
 * <b>Example usage:</b>
 * <pre>
 * &#064;Key("connection")
 * public interface ConnectionConfiguration {
 *    &#064;Key("url")
 *    String getUrl();
 *
 *    // &#064;IgnoreKey ignores the key implicitly defined by the getTimeout() property method.
 *    // connectionConfiguration.getTimeout().getConnectTimeout() resolves to the 'connection.read' key.
 *    // If &#064;IgnoreKey is removed, the configuration key will resolve to 'connection.timeout.read'.
 *    &#064;IgnoreKey
 *    TimeoutConfiguration getTimeout();
 * }
 *
 * public interface TimeoutConfiguration {
 *    &#064;Key("connect")
 *    int getConnectTimeout();
 *
 *    &#064;Key("read")
 *    int getReadTimeout();
 * }
 * </pre>
 *
 * @see Key
 * @see IgnorePrefix
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface IgnoreKey {
}
