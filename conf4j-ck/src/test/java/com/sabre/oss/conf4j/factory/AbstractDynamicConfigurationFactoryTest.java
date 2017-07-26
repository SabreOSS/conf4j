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

package com.sabre.oss.conf4j.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.sabre.oss.conf4j.annotation.DefaultValue;
import com.sabre.oss.conf4j.annotation.Internal;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.factory.model.collections.Component;
import com.sabre.oss.conf4j.factory.model.ignoreprefix.BaseConfiguration;
import com.sabre.oss.conf4j.internal.config.DynamicConfiguration;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public abstract class AbstractDynamicConfigurationFactoryTest<F extends AbstractConfigurationFactory> extends AbstractConfigurationFactoryTest<F> {
    protected final ConfigurationValuesSource mutationSource = spy(TestConfigurationValuesSource.class);

    @Override
    @Before
    public void before() {
        super.before();
        when(source.getValue(anyString())).thenReturn(absent());
        when(mutationSource.getValue(anyString())).thenReturn(absent());
    }

    @Test
    public void shouldReturnMutatedValueWhenMutationProviderIsAvailable() {
        // given
        when(mutationSource.getValue("keyPrefix.someProperty")).thenReturn(present("customValue"));
        // when
        Configuration config1 = factory.createConfiguration(Configuration.class, source);
        Configuration config2 = factory.createConfiguration(Configuration.class, source);
        ((DynamicConfiguration) config2).setConfigurationValuesSource(mutationSource);
        // then
        assertThat(config1.getSomeProperty()).isEqualTo("defaultValue");
        assertThat(config2.getSomeProperty()).isEqualTo("customValue");
    }

    @Test
    public void shouldNotAccessValuesSourceWhenConfigurationIsInstantiated() {
        // given
        when(source.getValue(anyString())).thenThrow(new IllegalStateException("ConfigurationValuesSource shouldn't be accessed"));
        // when
        factory.createConfiguration(BaseConfiguration.class, source);
    }

    @Test
    public void shouldBeDynamicAndProvideFreshValueFromTheSource() {
        reset(source);
        when(source.getValue(anyString())).thenReturn(absent());

        Component configuration = factory.createConfiguration(Component.class, source);

        // defaults when source doesn't provide any data
        assertThat(configuration.getName()).isEqualTo("defaultName");
        assertThat(configuration.getDescription()).isNull();
        assertThat(configuration.getSubComponents()).hasSize(2);
        assertThat(configuration.getSubComponents().get(0).getPropertyA()).isEqualTo("A0");
        assertThat(configuration.getSubComponents().get(0).getPropertyB()).isEqualTo("B0");
        assertThat(configuration.getSubComponents().get(1).getPropertyA()).isEqualTo("A");
        assertThat(configuration.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");


        // setup values source
        when(source.getValue("component.name")).thenReturn(present("name+"));
        when(source.getValue("component.description")).thenReturn(present("description+"));
        when(source.getValue("component.subComponent.size")).thenReturn(present("4"));
        when(source.getValue("component.subComponent[0].propertyA")).thenReturn(present("A0+"));
        when(source.getValue("component.subComponent[0].propertyB")).thenReturn(present("B0+"));
        when(source.getValue("component.subComponent[1].propertyA")).thenReturn(present("A1+"));
        when(source.getValue("component.subComponent[2].propertyA")).thenReturn(present("A2+"));
        when(source.getValue("component.subComponent[3].propertyA")).thenReturn(present("A3+"));

        // checks values from the source
        assertThat(configuration.getName()).isEqualTo("name+");
        assertThat(configuration.getDescription()).isEqualTo("description+");
        assertThat(configuration.getSubComponents()).hasSize(4);
        assertThat(configuration.getSubComponents().get(0).getPropertyA()).isEqualTo("A0+");
        assertThat(configuration.getSubComponents().get(0).getPropertyB()).isEqualTo("B0+");
        assertThat(configuration.getSubComponents().get(1).getPropertyA()).isEqualTo("A1+");
        assertThat(configuration.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");
        assertThat(configuration.getSubComponents().get(2).getPropertyA()).isEqualTo("A2+");
        assertThat(configuration.getSubComponents().get(2).getPropertyB()).isEqualTo("B");
        assertThat(configuration.getSubComponents().get(3).getPropertyA()).isEqualTo("A3+");
        assertThat(configuration.getSubComponents().get(3).getPropertyB()).isEqualTo("B");
    }

    @Test
    public void shouldRespectIgnoreKeyPrefixAnnotation() {
        // given
        when(source.getValue("subSubConfigurationProperty.subSubConfigurationClass.propertyA")).thenReturn(present("99"));
        // when
        BaseConfiguration configInstance = factory.createConfiguration(BaseConfiguration.class, source);
        // then
        assertThat(configInstance.getSubConfiguration().getSubSubConfiguration().getPropertyA()).isEqualTo(99);
    }

    @Test
    public void shouldReturnOnlyOneMutatedValue() {
        // given
        when(mutationSource.getValue("composite.sub.keyPrefix.someProperty")).thenReturn(present("customValue"));
        // when
        CompositeConfiguration config1 = factory.createConfiguration(CompositeConfiguration.class, source);
        AnotherCompositeConfig config2 = factory.createConfiguration(AnotherCompositeConfig.class, source);
        ((DynamicConfiguration) config1.getSubConfiguration()).setConfigurationValuesSource(mutationSource);
        ((DynamicConfiguration) config2.getSubConfiguration()).setConfigurationValuesSource(mutationSource);
        // then
        assertThat(config1.getSubConfiguration().getSomeProperty()).isNotEqualTo(config2.getSubConfiguration().getSomeProperty());
    }

    @Test
    public void shouldWorkWithParametrizedCollections() {
        // given
        when(source.getValue("component.subComponent[0].propertyA")).thenReturn(present("AAA1"));
        when(source.getValue("component.subComponent[1].propertyA")).thenReturn(present("AAA2"));
        // when
        Component configInstance = factory.createConfiguration(Component.class, source);
        // then
        assertThat(configInstance.getSubComponents()).hasSize(2);
        assertThat(configInstance.getSubComponents().get(0).getPropertyA()).isEqualTo("AAA1");
        assertThat(configInstance.getSubComponents().get(1).getPropertyA()).isEqualTo("AAA2");
    }

    @Test
    public void shouldWorkWithCompositeParametrizedCollections() {
        // given
        when(mutationSource.getValue("composite.collections.mapStringToListOfStrings")).thenReturn(present("{1:[p],2:[p,q],3:[p,q,z]}"));
        when(mutationSource.getValue("composite.collections.mapStringToMapStringToString")).thenReturn(present("{2:{a:1,b:2,c:3}}"));
        // when
        CompositeParametrisedCollectionsConfiguration defaultConfigInstance = factory.createConfiguration(CompositeParametrisedCollectionsConfiguration.class, source);
        CompositeParametrisedCollectionsConfiguration mutatedConfigInstance = factory.createConfiguration(CompositeParametrisedCollectionsConfiguration.class, source);
        ((DynamicConfiguration) mutatedConfigInstance).setConfigurationValuesSource(mutationSource);
        // then
        assertThat(defaultConfigInstance.getMapStringToListOfStrings()).hasSize(2).contains(
                entry("1", singletonList("a")),
                entry("2", asList("a", "b"))
        );
        assertThat(mutatedConfigInstance.getMapStringToListOfStrings()).hasSize(3).contains(
                entry("1", singletonList("p")),
                entry("2", asList("p", "q")),
                entry("3", asList("p", "q", "z"))
        );

        assertThat(defaultConfigInstance.getMapStringToMapStringToString()).isEqualTo(MapUtils.of("1", MapUtils.of("a", "b"), "2", MapUtils.of("c", "d", "e", "f")));
        assertThat(mutatedConfigInstance.getMapStringToMapStringToString()).isEqualTo(MapUtils.of("2", MapUtils.of("a", "1", "b", "2", "c", "3")));
    }

    @Test
    public void shouldNotSerializeAdditionalFields() throws JsonProcessingException {
        // given
        Configuration config = factory.createConfiguration(Configuration.class, source);
        // when
        String valueAsString = getObjectMapper().writeValueAsString(config);
        // then
        assertEquals("{\"someProperty\":\"defaultValue\"}", valueAsString);
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setAnnotationIntrospector(new InternalAwareJacksonAnnotationIntrospector());
        return objectMapper;
    }

    private static class InternalAwareJacksonAnnotationIntrospector extends JacksonAnnotationIntrospector {
        private static final long serialVersionUID = -2526224863963647647L;

        @Override
        protected boolean _isIgnorable(Annotated a) {
            return super._isIgnorable(a) || _hasAnnotation(a, Internal.class);
        }
    }

    @Key("anothercomposite")
    public interface AnotherCompositeConfig {
        @Key("sub")
        Configuration getSubConfiguration();
    }

    @Key("composite")
    public interface CompositeConfiguration {
        @Key("sub")
        Configuration getSubConfiguration();
    }

    @Key("composite.collections")
    public interface CompositeParametrisedCollectionsConfiguration {

        @Key
        @DefaultValue("{1:[a],2:[a,b]}")
        Map<String, List<String>> getMapStringToListOfStrings();

        @Key
        @DefaultValue("{1:{a:b},2:{c:d,e:f}}")
        Map<String, Map<String, String>> getMapStringToMapStringToString();
    }

    @Key("keyPrefix")
    public interface Configuration {
        @Key
        @DefaultValue("defaultValue")
        String getSomeProperty();
    }

}
