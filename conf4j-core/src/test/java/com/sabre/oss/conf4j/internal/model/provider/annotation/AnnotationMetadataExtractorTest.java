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

package com.sabre.oss.conf4j.internal.model.provider.annotation;

import com.sabre.oss.conf4j.annotation.DefaultsAnnotation;
import com.sabre.oss.conf4j.annotation.IgnoreKey;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.annotation.Meta;
import com.sabre.oss.conf4j.internal.model.provider.MetadataExtractor;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AnnotationMetadataExtractorTest {
    private static final MetadataExtractor extractor = AnnotationMetadataExtractor.getInstance();

    @Test
    public void shouldProvidePrefixForSubConfiguration() {
        // SubConfiguration class is not annotated with @Key - prefix should be extracted from the method.

        List<String> prefixes = extractor.getPrefixes(TestConfiguration.class, getMethod(TestConfiguration.class, "getSubConfiguration"));
        assertThat(prefixes).containsSequence("subConfiguration");

        prefixes = extractor.getPrefixes(TestConfiguration.class, getMethod(TestConfiguration.class, "getSubConfigurations"));
        assertThat(prefixes).containsSequence("subConfigurations");
    }

    @Test
    public void shouldProvidePrefixForSubConfigurationWithPrefix() {
        // @IgnorePrefix(Key) is applied which indicates prefix from method name should't be extracted.
        List<String> prefixes = extractor.getPrefixes(TestConfiguration.class, getMethod(TestConfiguration.class, "getSubConfigurationWithPrefix"));
        assertThat(prefixes).isEmpty();

        prefixes = extractor.getPrefixes(TestConfiguration.class, getMethod(TestConfiguration.class, "getSubConfigurationWithPrefixList"));
        assertThat(prefixes).isEmpty();
    }

    @Test
    public void shouldProvideDefaultValuesForSubConfigurationList() {
        //given
        //when
        List<Map<String, String>> values = extractor.getSubConfigurationListDefaultValues(TestConfiguration.class, getMethod(TestConfiguration.class, "getSubConfigurations"));

        // then
        assertThat(values).hasSize(2);

        assertThat(values.get(0)).contains(
                entry("one", "1"),
                entry("two", "first"));

        assertThat(values.get(1)).contains(
                entry("one", "2"),
                entry("two", "second"));
    }

    @Test
    public void shouldProvideDefaultValuesForSubConfiguration() {
        //given
        //when
        Map<String, String> values = extractor.getSubConfigurationDefaultValues(TestConfiguration.class, getMethod(TestConfiguration.class, "getSubConfiguration"));

        // then
        assertThat(values).hasSize(2);

        assertThat(values).contains(
                entry("one", "1"),
                entry("two", "the one"));
    }

    @Test
    public void shouldProvideAttributesForValueProperty() {
        //given

        //when
        Map<String, String> attributes = extractor.attributes(TestConfiguration.class, getMethod(TestConfiguration.class, "getUrl"));

        // then
        assertThat(attributes).containsExactly(
                entry("custom", "url"));
    }

    @Test
    public void shouldProvideAttributesForSubConfigurationProperty() {
        //given

        //when
        Map<String, String> attributes = extractor.attributes(TestConfiguration.class, getMethod(TestConfiguration.class, "getSubConfiguration"));

        // then
        assertThat(attributes).containsExactly(
                entry("custom", "sub-configuration"));
    }

    @Test
    public void shouldProvideAttributesForSubConfigurationListProperty() {
        //given

        //when
        Map<String, String> attributes = extractor.attributes(TestConfiguration.class, getMethod(TestConfiguration.class, "getSubConfigurations"));

        // then
        assertThat(attributes).containsExactly(
                entry("custom", "sub-configuration-list"));
    }

    @Test
    public void shouldProvideAttributesForType() {
        //given

        //when
        Map<String, String> attributes = extractor.attributes(TestConfiguration.class);

        // then
        assertThat(attributes).containsExactly(
                entry("type1", "value1"),
                entry("type2", "value2"));
    }

    @Test
    public void shouldProvideAttributesForCustomMetaAnnotations() {
        //given

        //when
        Map<String, String> attributes = extractor.attributes(TestConfiguration.class, getMethod(TestConfiguration.class, "getCustomMetaAnnotations"));

        // then
        assertThat(attributes).containsOnly(
                entry("fixed-one", "one"),
                entry("fixed-two", "two"),
                entry("custom", "custom-meta"),
                entry("attribute", "custom-attribute"),
                entry("another-attribute", "another-custom-attribute")
        );
    }

    @Meta(name = "type1", value = "value1")
    @Meta(name = "type2", value = "value2")
    public interface TestConfiguration {
        @Key
        @Meta(name = "custom", value = "url")
        String getUrl();

        @Default(one = "1", two = "the one")
        @Meta(name = "custom", value = "sub-configuration")
        SubConfiguration getSubConfiguration();

        @Default(one = "1", two = "first")
        @Default(one = "2", two = "second")
        @Meta(name = "custom", value = "sub-configuration-list")
        List<SubConfiguration> getSubConfigurations();

        @IgnoreKey
        SubConfigurationWithPrefix getSubConfigurationWithPrefix();

        @IgnoreKey
        List<SubConfigurationWithPrefix> getSubConfigurationWithPrefixList();

        @FixedMeta
        @CustomMeta("custom-meta")
        @CustomAttributes(attribute = "custom-attribute", anotherAttribute = "another-custom-attribute")
        String getCustomMetaAnnotations();
    }

    @Meta(name = "fixed-one", value = "one")
    @Meta(name = "fixed-two", value = "two")
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface FixedMeta {
    }

    @Meta(name = "custom")
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface CustomMeta {
        String value();
    }

    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface CustomAttributes {
        @Meta(name = "attribute")
        String attribute();

        @Meta(name = "another-attribute")
        String anotherAttribute();
    }


    @DefaultsAnnotation(Default.class)
    public interface SubConfiguration {
        @Key
        String getOne();

        @Key
        String getTwo();
    }

    @Key("prefix")
    public interface SubConfigurationWithPrefix {
        @Key
        String getOne();

        @Key
        String getTwo();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(Defaults.class)
    public @interface Default {
        String one();

        String two();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Defaults {
        Default[] value();
    }

    private Method getMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
