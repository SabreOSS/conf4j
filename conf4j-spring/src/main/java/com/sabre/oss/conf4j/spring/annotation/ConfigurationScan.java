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
 * Scans for configurations in the specified packages. It is usually used with spring's {@link Configuration} annotation.
 * <p>
 * <b>Example usage:</b>
 * </p>
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
 * &#064;ConfigurationScan(basePackageClasses = ConnectionConfiguration.class)
 * public class MyApplication {
 *   &#064;Autowired
 *   private ConnectionConfiguration connectionConfiguration;
 *   // ...
 *
 * }
 * </pre>
 * When you need greater control over scanning beans, use {@code <conf4j:configuration-scan .../>}
 * from <i>http://www.sabre.com/schema/oss/conf4j</i> namespace.
 *
 * @see Configuration
 * @see Component
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigurationScanRegistrar.class)
public @interface ConfigurationScan {
    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation
     * declarations e.g.: {@code @ConfigurationScan("org.my.pkg")} instead of
     * {@code @ConfigurationScan(basePackages="org.my.pkg")}.
     *
     * @return the list of base packages.
     */
    String[] value() default {};

    /**
     * Base packages to scan for configuration types.
     * <p>
     * {@link #value()} is an alias for (and mutually exclusive with) this attribute.
     * Use {@link #basePackageClasses()} for a type-safe alternative to String-based
     * package names.
     * </p>
     *
     * @return the list of base packages.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to
     * scan for annotated entities. The package of each class specified will be scanned.
     * <p>
     * Consider creating a special no-op marker class or interface in each package that
     * serves no purpose other than being referenced by this attribute.
     * </p>
     *
     * @return the list of classes which point to base packages.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Configuration annotations which are looked for while scanning the package(s) for configurations.
     * All configurations types annotated with specified annotations or any other annotations meta-annotated
     * with one of specified annotations will be discovered.
     * <p>
     * Default value is {@link Component}
     * </p>
     * Here is and example of the custom annotation which will be used for discovering configuration type candidates.
     * <pre>
     * &#064;Retention(RUNTIME)
     * &#064;Target(TYPE)
     * &#064;Documented
     * public &#064;interface MyConfiguration {
     * }
     *
     * &#064;ConfigurationScan(basePackageClasses = "com.foo.bar", configurationAnnotations = MyConfiguration.class)
     * public class SomeClass {
     *    //...
     * }
     * </pre>
     *
     * @return the list of annotations used for detecting configuration types.
     */
    Class<? extends Annotation>[] configurationAnnotations() default Component.class;
}
