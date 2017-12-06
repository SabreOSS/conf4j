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

import com.sabre.oss.conf4j.annotation.Meta.Metas;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines custom meta-attribute associated with a configuration property.
 * <p>
 * Custom meta-attribute is useful when there is a need to resolve a configuration
 * key to a value based on additional rules.
 * For example it can be used to specify a property file the configuration values source
 * should use. Of course there must be a logic in configuration values source implementation class which understands
 * such meta-attribute.
 * </p>
 * <pre>
 * public interface ConnectionConfiguration {
 *    &#064;Meta(name="file", value="connection.properties")
 *    String getUrl();
 * }
 * </pre>
 * <p>
 * When {@code @Meta} annotation is applied to configuration type it will be inherited
 * by all configuration properties.
 * </p>
 * <pre>
 * &#064;Meta(name="file", value="connection.properties")
 * public interface ConnectionConfiguration {
 *    String getUrl();
 *    String getUser();
 * }
 * </pre>
 * It is possible to override a meta-data attribute defined on a configuration type level by applying {@code @Meta}
 * on the property method:
 * <pre>
 * &#064;Meta(name="file", value="connection.properties")
 * public interface ConnectionConfiguration {
 *    // 'url' property inherits 'file' meta-data attribute from the @Meta applied on the configuration type level.
 *    // In this case 'file' attribute's value is 'connection.properties'.
 *    String getUrl();
 *
 *    // 'user' property overrides the value of 'file' meta-data attribute and it is 'user.properties'.
 *    &#064;Meta(name="file", value="users.properties")
 *    String getUser();
 * }
 * </pre>
 * In general all meta-data attributes defined on the higher level in the hierarchy
 * (for example by the type which owns the property; super type when one configuration extends another configuration;
 * super property method when property overrides property declared in the supper type) are collected and merged.
 * <p>
 * {@code @Meta} can be used to meta-annotate another annotations. It allows creating custom annotations which
 * define custom meta-data in more expressive way.
 * <p>
 * There are three possible forms of custom meta-data annotations.
 * </p>
 * <p>
 * The custom annotation has fixed set of meta-data attributes assigned. it is annotated
 * with any number of {@code @Meta} annotations and cannot declare any attribute.
 * </p>
 * <pre>
 * &#064;Meta(name="file", value="application.properties")
 * &#064;Retention(RUNTIME)
 * &#064;Target({TYPE, METHOD})
 * &#064;Documented
 * public &#064;interface ApplicationProperties {
 * }
 *
 * // meta-data attribute 'file' is applied to MyConfiguration and it has the same effect as
 * // &#064;Meta(name="file", value="application.properties")
 * &#064;ApplicationProperties
 * public interface MyConfiguration {
 *    // ...
 * }
 * </pre>
 * <p>
 * The custom annotation is annotated with exactly one {@code @Meta}. Attribute's name is provided by {@code @Meta},
 * the value must be left unspecified (for example {@code @Meta(name="attribute-name")}).
 * The value is defined by the custom annotation attribute.
 * The custom annotation must define exactly one attribute and its type cannot be <em>Class</em>, <em>array</em>
 * nor <em>another annotation</em>. In most cases the type is {@code String}, but enumerations and primitive types
 * like {@code boolean} or {@code integer} are supported. {@code toString()} method is used to convert annotation's
 * attribute value to {@code String}.
 * </p>
 * <pre>
 * &#064;Meta(name="file")
 * &#064;Retention(RUNTIME)
 * &#064;Target({TYPE, METHOD})
 * &#064;Documented
 * public &#064;interface SourceFile {
 *    String value();
 * }
 *
 * // meta-data attribute 'file' is applied to MyConfiguration and it has the same effect as
 * // &#064;Meta(name="file", value="application.properties")
 * &#064;SourceFile("application.properties")
 * public interface MyConfiguration {
 *    // ...
 * }
 * </pre>
 * <p>
 * The last option is to apply {@code @Meta} annotation on the custom annotation attributes. It allows
 * defining multiple meta-data attributes by one annotation. All attributes of the custom annotation must
 * be annotated with {@code @Meta} and, as in previous option, it is not allowed to specify value by {@code @Meta}
 * nor declare an attribute which type is <em>Class</em>, <em>array</em> nor <em>another annotation</em>.
 * </p>
 * <pre>
 * &#064;Retention(RUNTIME)
 * &#064;Target({TYPE, METHOD})
 * &#064;Documented
 * public &#064;interface Source {
 *    &#064;Meta(name="name")
 *    String name();
 *    &#064;Meta(name="format")
 *    String format();
 * }
 *
 * // meta-data attribute 'file' is applied to MyConfiguration and it has the same effect as
 * // using two &#064;Meta annotations:
 * // &#064;Meta(name="name", value="application") and &#064;Meta(name="format", value="xml")
 * &#064;Source(name="application", format="xml")
 * public interface MyConfiguration {
 *    // ...
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Repeatable(Metas.class)
@Documented
public @interface Meta {
    /**
     * Specifies meta-attribute name.
     *
     * @return meta-attribute name.
     */
    String name();

    /**
     * Specifies meta-attribute value.
     *
     * @return meta-attribute property name and value.
     */
    String value() default "";

    /**
     * Container annotation that aggregates several {@link Meta} annotations.
     * <p>
     * It should be used in conjunction with Java 8's support for <em>repeatable annotations</em>,
     * where {@link Meta} can simply be declared several times on the same
     * {@linkplain ElementType#TYPE type}, implicitly generating this container annotation.
     * </p>
     */
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    @interface Metas {
        Meta[] value();
    }
}

