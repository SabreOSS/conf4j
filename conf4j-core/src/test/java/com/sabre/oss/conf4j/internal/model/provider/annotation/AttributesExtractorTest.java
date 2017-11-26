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

package com.sabre.oss.conf4j.internal.model.provider.annotation;

import com.sabre.oss.conf4j.annotation.Configuration;
import com.sabre.oss.conf4j.annotation.Meta;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import static com.sabre.oss.conf4j.internal.model.provider.annotation.AttributesExtractor.getMetaAttributes;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AttributesExtractorTest {

    @Test
    public void shouldExtractAttributesFromFixedAnnotation() {
        // when
        Map<String, String> attributes = getMetaAttributes(FixedAttributesUsage.class);

        // then
        assertThat(attributes).containsOnly(
                entry("one", "value-one"),
                entry("two", "value-two")
        );
    }

    @Test
    public void shouldExtractAttributesFromFixedAnnotationForMethod() {
        // when
        Map<String, String> attributes = getMetaAttributes(getMethod(FixedAttributesUsage.class, "fixedAnnotationOnly"));

        // then
        assertThat(attributes).containsOnly(
                entry("one", "value-one"),
                entry("two", "value-two")
        );
    }

    @Test
    public void shouldExtractAttributesFromFixedAnnotationAndMetaForMethod() {
        // when
        Map<String, String> attributes = getMetaAttributes(getMethod(FixedAttributesUsage.class, "fixedAnnotationAndMeta"));

        // then
        assertThat(attributes).containsOnly(
                entry("one", "value-one"),
                entry("two", "value-two"),
                entry("another-meta", "anotherValue")
        );
    }

    // - names and values are fixed and cannot be customized
    // - annotation must be argument-less
    // - any number of @Meta can be applied
    @Meta(name = "one", value = "value-one")
    @Meta(name = "two", value = "value-two")
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    @interface FixedNameAndValue {
    }

    @FixedNameAndValue
    interface FixedAttributesUsage {
        @FixedNameAndValue
        String fixedAnnotationOnly();

        @FixedNameAndValue
        @Meta(name = "another-meta", value = "anotherValue")
        String fixedAnnotationAndMeta();
    }

    @Test
    public void shouldExtractAttributesFromCustomizableForType() {
        // when
        Map<String, String> attributes = getMetaAttributes(CustomizableValueUsage.class);

        // then
        assertThat(attributes).containsOnly(
                entry("name", "type-level")
        );
    }

    @Test
    public void shouldExtractAttributesFromCustomizableForMethod() {
        // when
        Map<String, String> attributes = getMetaAttributes(getMethod(CustomizableValueUsage.class, "customizableOnly"));

        // then
        assertThat(attributes).containsOnly(
                entry("name", "method-level")
        );
    }

    @Test
    public void shouldExtractAttributesFromCustomizableAndMetaForMethod() {
        // when
        Map<String, String> attributes = getMetaAttributes(getMethod(CustomizableValueUsage.class, "customizableAndMeta"));

        // then
        assertThat(attributes).containsOnly(
                entry("name", "method-level-with-additional-meta"),
                entry("another-meta", "anotherValue")
        );
    }

    // - name is specified by @Meta and it's fixed
    // - value is specified by value() attribute, but it can also be any other attribute of any type except Annotation and array.
    // - only one @Meta annotation is allowed
    @Meta(name = "name")
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface CustomizableValue {
        String value();
    }

    @CustomizableValue("type-level")
    interface CustomizableValueUsage {
        @CustomizableValue("method-level")
        String customizableOnly();

        @CustomizableValue("method-level-with-additional-meta")
        @Meta(name = "another-meta", value = "anotherValue")
        String customizableAndMeta();
    }

    @Test
    public void shouldExtractAttributesFromMultipleAttributesForType() {
        // when
        Map<String, String> attributes = getMetaAttributes(MultipleAttributesUsage.class);

        // then
        assertThat(attributes).containsOnly(
                entry("first", "first-attribute-value"),
                entry("second", "second-attribute-value")
        );
    }

    @Test
    public void shouldExtractAttributesFromMultipleAttributesForMethod() {
        // when
        Map<String, String> attributes = getMetaAttributes(getMethod(MultipleAttributesUsage.class, "customizableOnly"));

        // then
        assertThat(attributes).containsOnly(
                entry("first", "first-attribute-value"),
                entry("second", "second-attribute-value")
        );
    }

    @Test
    public void shouldExtractAttributesFromMultipleAttributesAndMetaForMethod() {
        // when
        Map<String, String> attributes = getMetaAttributes(getMethod(MultipleAttributesUsage.class, "customizableAndMeta"));

        // then
        assertThat(attributes).containsOnly(
                entry("first", "first-attribute-value"),
                entry("second", "defaultSecondValue"),
                entry("another-meta", "anotherValue-meta")
        );
    }

    // - @Meta annotation is applied only to attribute value
    // - name is specified by @Meta on attribute, value by the value from attribute
    // - only one @Meta annotation can be applied on attribute
    // - all attributes must be annotated
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface MultipleAttributes {
        @Meta(name = "first")
        String first();

        @Meta(name = "second")
        String second() default "defaultSecondValue";
    }

    @MultipleAttributes(first = "first-attribute-value", second = "second-attribute-value")
    interface MultipleAttributesUsage {
        @MultipleAttributes(first = "first-attribute-value", second = "second-attribute-value")
        String customizableOnly();

        @MultipleAttributes(first = "first-attribute-value")
        @Meta(name = "another-meta", value = "anotherValue-meta")
        String customizableAndMeta();
    }

    @Test
    public void shouldExtractAttributesFromManyAnnotationsForMethod() {
        // when
        Map<String, String> attributes = getMetaAttributes(MultipleAnnotationsUsage.class);

        // then
        assertThat(attributes).containsOnly(
                entry("one", "value-one"),
                entry("two", "value-two"),
                entry("name", "custom-value"),
                entry("first", "first"),
                entry("second", "second"),
                entry("meta", "custom-meta")
        );
    }

    @FixedNameAndValue
    @CustomizableValue("custom-value")
    @MultipleAttributes(first = "first", second = "second")
    @Meta(name = "meta", value = "custom-meta")
    interface MultipleAnnotationsUsage {
    }

    @Test
    public void shouldFindAllMetaDataInTheHierarchy() {
        Map<String, String> attributes;

        attributes = getMetaAttributes(BaseConfiguration.class);
        assertThat(attributes).containsOnly(
                entry("name", "type-base"),
                entry("base", "type-base")
        );

        attributes = getMetaAttributes(ChildConfiguration.class);
        assertThat(attributes).containsOnly(
                entry("name", "type-child"),
                entry("base", "type-base"),
                entry("child", "type-base")
        );

        attributes = getMetaAttributes(getMethod(BaseConfiguration.class, "getValue"));
        assertThat(attributes).containsOnly(
                entry("name", "property-base"),
                entry("meta", "property-base")
        );

        attributes = getMetaAttributes(getMethod(ChildConfiguration.class, "getAnotherValue"));
        assertThat(attributes).containsOnly(
                entry("name", "another-property-child")
        );

        attributes = getMetaAttributes(getMethod(ChildConfiguration.class, "getAnotherValue"));
        assertThat(attributes).containsOnly(
                entry("name", "another-property-child")
        );
    }

    @Configuration
    @CustomizableValue("type-base")
    @Meta(name = "base", value = "type-base")
    interface BaseConfiguration {
        @CustomizableValue("property-base")
        @Meta(name = "meta", value = "property-base")
        String getValue();
    }

    @Configuration
    @CustomizableValue("type-child")
    @Meta(name = "child", value = "type-base")
    interface ChildConfiguration extends BaseConfiguration {
        @Override
        @CustomizableValue("property-child")
        @Meta(name = "additional-meta", value = "meta")
        String getValue();

        @CustomizableValue("another-property-child")
        String getAnotherValue();
    }

    @Test
    public void shouldFailIfNotAllAttributesAreAnnotated() {
        assertThrows(IllegalArgumentException.class, () -> {
            getMetaAttributes(NotAllAttributesAreAnnotatedUsage.class);
        }, "All @com.sabre.oss.conf4j.internal.model.provider.annotation.AttributesExtractorTest$NotAllAttributesAreAnnotated(annotated=annotatedValue, notAnnotated=notAnnotatedValue) " +
                "annotations attributes must be annotated with @com.sabre.oss.conf4j.annotation.Meta.");
    }

    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface NotAllAttributesAreAnnotated {
        @Meta(name = "annotated")
        String annotated();

        // @Meta annotation is missing
        String notAnnotated();
    }

    @NotAllAttributesAreAnnotated(annotated = "annotatedValue", notAnnotated = "notAnnotatedValue")
    interface NotAllAttributesAreAnnotatedUsage {
    }

    @Test
    public void shouldFailIfMoreThanAttributeIsDefined() {
        assertThrows(IllegalArgumentException.class, () -> {
            getMetaAttributes(TooManyAttributesUsage.class);
        }, "@com.sabre.oss.conf4j.internal.model.provider.annotation.AttributesExtractorTest$TooManyAttributes(attribute=attribute, anotherAttribute=anotherAttribute) " +
                "annotation is meta-annotated with @com.sabre.oss.conf4j.annotation.Meta and define more than one attribute: " +
                "anotherAttribute, attribute ");
    }

    @Meta(name = "annotated")
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface TooManyAttributes {
        String attribute();

        // Only one attribute is expected
        String anotherAttribute();
    }

    @TooManyAttributes(attribute = "attribute", anotherAttribute = "anotherAttribute")
    interface TooManyAttributesUsage {
    }

    @Test
    public void shouldFailIfInvalidAttributeType() {
        assertThrows(IllegalArgumentException.class, () -> {
            getMetaAttributes(InvalidAttributeTypeUsage.class);
        }, "@com.sabre.oss.conf4j.internal.model.provider.annotation.AttributesExtractorTest$InvalidAttributeType(attribute=[1, 2]) " +
                "annotation is meta-annotated with @com.sabre.oss.conf4j.annotation.Meta and its attribute 'attribute' " +
                "type is [Ljava.lang.String;. Only scalar, simple types are supported.");
    }

    @Meta(name = "annotated")
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface InvalidAttributeType {
        String[] attribute();
    }

    @InvalidAttributeType(attribute = {"1", "2"})
    interface InvalidAttributeTypeUsage {
    }

    private static Method getMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " doesn't declare parameterless method " + methodName);
        }

    }

    private static final class MatchesPattern extends TypeSafeMatcher<String> {
        private final Pattern pattern;

        private MatchesPattern(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        protected boolean matchesSafely(String item) {
            return pattern.matcher(item).matches();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a string matching the pattern '" + pattern + '\'');
        }
    }
}
