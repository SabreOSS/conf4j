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
 * This annotation allows resetting the key prefixes defined by the {@link Key} annotation in parent configurations.
 * <p>
 * {@code @IgnorePrefix} is handy when there is a need to adjust the keys for the value property or sub-configuration
 * locally, without affecting other properties. The common use case is coupling {@code Key} together
 * with {@code @IgnorePrefix}. The first annotation will set a key prefix, the second will disable all prefixes
 * defined by parent configurations.
 * </p>
 * <p>
 * {@code @IgnorePrefix} together with {@code @Key} are similar to {@link FallbackKey}, but the main
 * difference is the {@code @FallbackKey} first checks the standard keys, and then falls back to the key specified as
 * its parameter.
 * </p>
 * <b>Example usage:</b>
 * <pre>
 * &#064;Key("connection")
 * public interface ConnectionConfiguration {
 *    &#064;Key("url")
 *    String getUrl();
 *
 *    // &#064;IgnorePrefix disables prefixes defined in the parent configuration ConnectionConfiguration -
 *    // 'connection' prefix is ignored. Only prefix 'timeout' is used so connectionConfiguration.getTimeout().getConnectTimeout()
 *    // resolves to key 'timeout.read'.
 *    // If &#064;IgnorePrefix is removed, the configuration key will resolve to 'connection.timeout.read'.
 *    &#064;IgnorePrefix
 *    &#064;Key("timeout")
 *    TimeoutConfiguration getTimeout();
 * }
 *
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
 * @see FallbackKey
 * @see IgnoreKey
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface IgnorePrefix {
}
