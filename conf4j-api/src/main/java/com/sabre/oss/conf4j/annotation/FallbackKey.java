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
 * Specifies an additional fallback, global configuration key as a source of the property value. It is used when the value
 * associated with the key defined by {@link Key} annotation is not available.
 * The key specified by {@code &#064;FallbackKey} is <i>absolute</i>. No other prefixes defined by {@link Key}
 * on any level is applied.
 * <p>
 * {@code &#064;FallbackKey} is used primarily to maintain backward compatibility when the original key cannot be changed
 * in the underlying data source.
 * <p>
 * <b>Example usage:</b>
 * <pre>
 * public interface ConnectionConfiguration {
 *    // first 'url' key is checked, if the value is not available then 'global.url' key is used
 *    &#064;Key("url")
 *    &#064;FallbackKey("global.url")
 *    String getUrl();
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface FallbackKey {
    /**
     * Defines the global (absolute), callback key value used as a fallback in the instance a configuration
     * does not have configuration value assigned with a key defined by {@link Key} annotation.
     *
     * @return global (absolute) key
     */
    String value();
}
