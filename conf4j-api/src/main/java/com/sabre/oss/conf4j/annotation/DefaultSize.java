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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation specifies the default list size and applies only to the configuration property, which returns a
 * list of sub-configurations. The default size is used only when the configuration value source does not provide
 * a value for the list size key.
 * <p>
 * {@code @DefaultSize} is usually used when there are default values defined for the list (using annotation
 * declared by &#064;{@link DefaultsAnnotation}). It defines the size of the list
 * (in absence of the value in configuration source) and allows sub-configuration elements to be populated in the list
 * from default values.
 * <p><b>Example usage:</b>
 * <pre>
 * &#064;DefaultsAnnotation(DefaultTimeout.class)
 * public interface TimeoutConfiguration {
 *     &#064;Key("connect")
 *     &#064;DefaultValue("10000")
 *     int getConnectTimeout();
 *
 *     &#064;Key("read")
 *     &#064;DefaultValue("5000")
 *     int getReadTimeout();
 * }
 *
 * &#064;Target(METHOD)
 * &#064;Retention(RUNTIME)
 * &#064;Repeatable(DefaultTimeouts.class)
 * &#064;Documented
 * public &#064;interface DefaultTimeout {
 *     String connectTimeout();
 *     String readTimeout() default "3000";
 * }
 *
 * &#064;Target(METHOD)
 * &#064;Retention(RUNTIME)
 * &#064;Documented
 * public &#064;interface DefaultTimeouts {
 *     DefaultTimeout[] value();
 * }
 *
 * public interface TimeoutsConfiguration {
 *    // Default values for the list of sub-configurations. &#064;DefaultSize(2) indicates the size of the list
 *    // will be <i>2</i> if the configuration value source does not provide a value for the list size.
 *    // The elements in the list will be populated from values provided by value source or, if not available,
 *    // from defaults. When value source provides the value for the list size &#064;DefaultSize(2) is ignored.
 *    &#064;DefaultSize(2)
 *    &#064;Key("otherTimeouts")
 *    &#064;DefaultTimeout(connectTimeout = "100", readTimeout = "200")
 *    &#064;DefaultTimeout(connectTimeout = "1000", readTimeout = SKIP)
 *    &#064;DefaultTimeout(connectTimeout = "4000", readTimeout = "8000")
 *   {@code List<TimeoutConfiguration>} getOtherTimeouts();
 * }
 * </pre>
 *
 * @see DefaultsAnnotation
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface DefaultSize {
    /**
     * Specifies the default size of the list of sub-configurations. Value cannot be negative.
     *
     * @return default size of sub-configurations.
     */
    int value();
}
