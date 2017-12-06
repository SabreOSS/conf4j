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

package com.sabre.oss.conf4j.spring.annotation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Registers specified configuration type(s) in the spring context.
 * <p>
 * <b>Example usage:</b>
 * <pre>
 * &#064;Component
 * public interface ConnectionConfiguration {
 *     &#064;Key
 *     String getUrl();
 *     // ...
 * }
 *
 * &#064;Configuration
 * &#064;EnableConf4j
 * &#064;ConfigurationType(ConnectionConfiguration.class)
 * public class MyApplication {
 *   &#064;Autowired
 *   private ConnectionConfiguration connectionConfiguration;
 *   // ...
 * }
 * </pre>
 * <p>
 * There is a corresponding {@code <conf4j:configuration .../>} tag for xml based spring configurations
 * in <i>http://www.sabre.com/schema/oss/conf4j</i> namespace.
 * <p>
 * When there is a need to discover all configuration types in the specified packages, use {@link ConfigurationScan}.
 *
 * @see ConfigurationScan
 * @see Configuration
 * @see Component
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConfigurationTypes.class)
@Documented
@Import(ConfigurationTypeRegistrar.class)
public @interface ConfigurationType {
    /**
     * Specifies configuration type to register in the spring context.
     *
     * @return the configuration type.
     */
    Class<?> value();

    /**
     * The name of the bean the configuration type is registered, or if plural, the name of the bean followed by
     * the aliases for this configuration type.
     * If left unspecified the name of the bean is the fully qualified configuration class name.
     *
     * @return the configuration name (and aliases).
     */
    String[] name() default {};
}
