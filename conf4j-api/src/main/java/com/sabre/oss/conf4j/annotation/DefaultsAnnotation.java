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

import java.lang.annotation.*;


/**
 * This annotation allows you to define default values for the configuration when the configuration is used as a sub-configuration.
 * {@code @DefaultsAnnotation} does not define the defaults directly, it simply specifies which annotation should be
 * used for that purpose.
 * <p>Usage of this annotation requires the following steps:</p>
 * <ol>
 * <li>Create an auxiliary annotation which holds the default values for the configuration.
 * The target must be {@code @Target(METHOD)} and the retention policy {@code @Retention(RUNTIME)}.
 * It is also advised to specify {@code @Documented} the default values as visible in the generated javadoc.
 * The annotation must define the list of attributes that match the properties defined by the configuration, but the type
 * of each attribute must be {@code String}.
 * <i>Note:</i> Currently there is no validation that ensures the annotation attributes and configuration
 * properties match, but this may change in the future.
 * </li>
 * <li>If a sub-configuration is designed to be used in the lists, create an additional annotation which is a wrapper
 * over the list of auxiliary annotations and register with &#064;{@link Repeatable}. It allows a repeating auxiliary
 * annotation without the wrapper annotation. It also tells the framework the type of wrapping annotation to use.</li>
 * <li>Annotate the configuration class with {@code @DefaultsAnnotation} and specify (as an {@code value} argument)
 * the auxiliary annotation as a source of defaults.</li>
 * <li>Annotate the configuration's getter, which provides sub-configuration or a list of sub-configurations, with the
 * auxiliary annotation(s) then provide default values.</li>
 * </ol>
 * Use {@link #NULL} constant to declare default value {@code null}.
 * Use {@link #SKIP} constant to skip overriding default value declared on the sub-configuration.
 * <p><b>Example usage:</b></p>
 * <pre>
 * // Sub-configuration default values can be changed by &#064;DefaultTimeout and &#064;DefaultTimeouts annotations
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
 * // An auxiliary annotation which holds default values.
 * &#064;Target(METHOD)
 * &#064;Retention(RUNTIME)
 * // &#064;Repeatable annotation (introduced in java 8) allows repeating &#064;DefaultTimeouts without the wrapper list annotation.
 * &#064;Repeatable(DefaultTimeouts.class)
 * &#064;Documented
 * public &#064;interface DefaultTimeout {
 *     // Annotation must declare only attributes which correspond to the properties of the configuration class.
 *     // Please note the type of the property is a string.
 *     String connectTimeout();
 *     // A default value can be assigned and it will be used in the instance the value for an attribute is not provided.
 *     String readTimeout() default "3000";
 * }
 *
 * // A wrapper annotation for an array of &#064;DefaultTimeout. This is used for a list of sub-configurations.
 * &#064;Target(METHOD)
 * &#064;Retention(RUNTIME)
 * &#064;Documented
 * public &#064;interface DefaultTimeouts {
 *     DefaultTimeout[] value();
 * }
 *
 * // usage of default values annotations
 * public interface ConnectionConfiguration {
 *    // Regular configuration property
 *    &#064;Key("url")
 *    String getUrl();
 *
 *    // Default values are defined in TimeoutConfiguration by &#064;DefaultValue
 *    &#064;Key("standard")
 *    TimeoutConfiguration getStandardTimeout();
 *
 *    // Default values are overridden, source of default is &#064;DefaultTimeout annotation
 *    &#064;Key("default")
 *    &#064;DefaultTimeout(connectTimeout = "100", readTimeout = "200")
 *    TimeoutConfiguration getDefaultTimeout();
 *
 *    // Default values for the list of sub-configurations. Because there are three &#064;DefaultTimeout annotations,
 *    // the minimum size of the list is 3. You can also use &#064;DefaultSize to specify the default of the list size
 *    // to reduce or enhance the number of elements in the list. In the latter case, default values are fetched from
 *    // the sub-configuration.
 *    // SKIP in the second &#064;DefaultTimeout annotation indicates the default value is fetched from the &#064;DefaultValue
 *    // applied on TimeoutConfiguration.getReadTimeout.
 *    &#064;Key("otherTimeouts")
 *    &#064;DefaultTimeout(connectTimeout = "100", readTimeout = "200")
 *    &#064;DefaultTimeout(connectTimeout = "1000", readTimeout = SKIP)
 *    &#064;DefaultTimeout(connectTimeout = "4000", readTimeout = "8000")
 *   {@code List<TimeoutConfiguration>} getOtherTimeouts();
 * }
 * </pre>
 *
 * @see DefaultValue
 * @see DefaultSize
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface DefaultsAnnotation {
    /**
     * Used to specify the default value {@code null}.
     * <p>For a type that doesn't support {@code null} like primitives, using {@code NULL} is a bug.</p>
     *
     * @see DefaultValue#NULL
     */
    String NULL = DefaultValue.NULL;

    /**
     * Used to specify the default value declared in the sub-configuration should not be overridden.
     *
     * @see #NULL
     * @see DefaultValue#NULL
     */
    String SKIP = Constants.SKIP;

    /**
     * Specifies an annotation which holds default values for sub-configuration.
     *
     * @return the class of an annotation which holds default values.
     */
    Class<? extends Annotation> value();
}
