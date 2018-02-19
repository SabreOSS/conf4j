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

package com.sabre.oss.conf4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates properties with encrypted values that must be decrypted before returning to the client.
 * <p>
 * The library does not provide a default implementation for this feature at runtime. See configuration factory
 * for details of how to setup this feature.
 * <p>
 * {@code @Encrypted} annotation can be used for configuration properties. It is not supported
 * for sub-configurations, nor a list of sub-configurations.
 * <p>
 * <b>Example usage:</b>
 * <pre>
 * public interface ConnectionConfiguration {
 *    &#064;Key
 *    String getUrl();
 *
 *    &#064;Key
 *    String getUser();
 *
 *    // password property value is encrypted
 *    // default encryption provider is used to decrypt the value
 *    &#064;Key
 *    &#064;Encrypted
 *    String getPassword();
 * }
 * </pre>
 */
@Inherited
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface Encrypted {
    /**
     * Constant value {@code default} which indicates default encryption provider.
     */
    String DEFAULT = "default";

    /**
     * The encryption provider name is used when the configuration value is being decrypted.
     * If the encryption provider name is not specified, then the encryption provider,
     * with a name defined by {@link #DEFAULT}, is used.
     * <p>
     * If there is no suitable encryption provider, an exception is thrown for the configuration implementation
     * at build-time, or when the configuration property is accessed.
     *
     * @return encryption provider name.
     */
    String value() default DEFAULT;
}
