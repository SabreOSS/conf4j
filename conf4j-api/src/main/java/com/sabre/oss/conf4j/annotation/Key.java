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
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a configuration key for a configuration property in a configuration class.
 * For hierarchical configurations the final property key is a concatenation of keys from all configuration
 * levels and above, starting with the root configuration.
 * <p>
 * {@code @Key} annotation is applied to the configuration type or the property method. When it is applied in the type
 * or on the property which returns sub-configurations or a list of sub-configurations, it specifies a key prefix
 * which is appended to the children properties. When {@code @Key} is applied to the value property
 * (where the type is not another configuration or list of configurations), it specifies the final component of the key.
 * <p>
 * {@code @Key} can specify multiple keys (or prefixes). Each key is applied in the sequence
 * until the configuration value associated with the resulting key is found. When {@code value} is not specified,
 * the property name (when the annotation is applied on the getter) or uncapitalized short class name is used.
 * <p>
 * <i>Note</i>: Specifying {@code @Key} (without any keys) on the value property (which type is not another
 * configuration nor list of configurations) is optional in default <i>non-strict</i> configuration model provider.
 * If <i>strict</i> provider is used, every value configuration property must be annotated.
 * <p>
 * <b>Example usage:</b>
 * <pre>
 * // defines a 'connection' prefix which is applied for all properties defined by the configuration type
 * &#064;Key("connection")
 * public interface ConnectionConfiguration {
 *    // When key doesn't specify value explicitly, property name is used.
 *    // Full key associated with the property is 'connection.url',
 *    // 'connection' prefix is from the &#064;Key annotation type.
 *    &#064;Key
 *    String getUrl();
 *
 *    // Key name is explicitly specified, the full key is 'connection.timeout'.
 *    &#064;Key("timeout")
 *    int getConnectionTimeout();
 *
 *    // Multiple keys are specified, the key set of the property is 'connection.defaultTimeout'
 *    // and 'connection.standardTimeout'.
 *    &#064;Key({"defaultTimeout", "standardTimeout"})
 *    int getDefaultTimeout();
 * }
 * </pre>
 *
 * @see FallbackKey
 * @see IgnorePrefix
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Documented
public @interface Key {
    /**
     * Specifies configuration key names or key prefixes. When the value is empty (by default)
     * either the property name (when the annotation is applied to the method), or uncapitalized short class name (when
     * the annotation is applied on the type), will be used.
     *
     * @return configuration keys.
     */
    String[] value() default {};
}
