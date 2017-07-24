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
package com.sabre.oss.conf4j.factory;

import com.sabre.oss.conf4j.annotation.AbstractConfiguration;
import com.sabre.oss.conf4j.annotation.DefaultValue;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.source.OptionalValue;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public abstract class AbstractParametrizedConfigurationTest<F extends AbstractConfigurationFactory> extends AbstractBaseConfigurationFactoryTest<F> {
    @Test
    public void shouldSupportParameterizedConfiguration() {
        when(source.getValue(anyString())).thenReturn(OptionalValue.absent());
        when(source.getValue("stringValidation.validationName")).thenReturn(present("stringValidation"));
        when(source.getValue("stringValidation.validationRule.pattern")).thenReturn(present(".*"));
        when(source.getValue("intValidation.validationName")).thenReturn(present("intValidation"));
        when(source.getValue("intValidation.validationRule.min")).thenReturn(present("1"));
        when(source.getValue("intValidation.validationRule.max")).thenReturn(present("2"));

        StringValidationConfiguration stringValidation = factory.createConfiguration(StringValidationConfiguration.class, source);
        IntValidationConfiguration intValidation = factory.createConfiguration(IntValidationConfiguration.class, source);

        assertThat(stringValidation.getValidationName()).isEqualTo("stringValidation");
        assertThat(stringValidation.getValidationRule().getPattern()).isEqualTo(".*");
        assertThat(stringValidation.getDefaultValue()).isNull();

        assertThat(intValidation.getValidationName()).isEqualTo("intValidation");
        assertThat(intValidation.getValidationRule().getMin()).isEqualTo(1);
        assertThat(intValidation.getValidationRule().getMax()).isEqualTo(2);
        assertThat(intValidation.getDefaultValue()).isNull();
    }

    /**
     * This is an abstract, a base configuration which is parametrized by generic type T.
     * T is a sub-configuration like StringValidationRule or IntegerValidationRule.
     */
    @AbstractConfiguration
    public interface AbstractValidationConfiguration<T, V> {
        @Key
        String getValidationName();

        @Key
        @DefaultValue("true")
        boolean isMandatory();

        T getValidationRule();

        @Key
        V getDefaultValue();
    }

    /**
     * This configuration inherits all properties from the AbstractValidationConfiguration and specifies
     * the sub-configuration type as StringValidationRule.
     */
    @Key("stringValidation")
    public interface StringValidationConfiguration extends AbstractValidationConfiguration<StringValidationRule, String> {
    }

    @Key("intValidation")
    public interface IntValidationConfiguration extends AbstractValidationConfiguration<IntegerValidationRule, Integer> {
    }

    public interface StringValidationRule {
        @Key
        String getPattern();
    }

    public interface IntegerValidationRule {
        @Key
        Integer getMin();

        @Key
        Integer getMax();
    }

    @Test
    public void shouldSupportParameterizedSubConfigurationList() {
        when(source.getValue(anyString())).thenReturn(OptionalValue.absent());
        when(source.getValue("stringValidationRules.validationRules.size")).thenReturn(present("2"));
        when(source.getValue("stringValidationRules.validationRules[0].pattern")).thenReturn(present("0.*"));
        when(source.getValue("stringValidationRules.validationRules[1].pattern")).thenReturn(present("1.*"));

        StringValidationRules stringValidationRules = factory.createConfiguration(StringValidationRules.class, source);

        assertThat(stringValidationRules.getValidationRules()).hasSize(2);
        assertThat(stringValidationRules.getValidationRules().get(0).getPattern()).isEqualTo("0.*");
        assertThat(stringValidationRules.getValidationRules().get(1).getPattern()).isEqualTo("1.*");
    }

    @AbstractConfiguration
    @Key
    public interface AbstractValidationRules<C, S, V> {
        List<C> getValidationRules();
    }

    public interface StringValidationRules extends AbstractValidationRules<StringValidationRule, Integer, String> {
    }

    @Test
    public void shouldSupportParameterizedValueProperties() {
        when(source.getValue(anyString())).thenReturn(OptionalValue.absent());
        when(source.getValue("propertyConfiguration.property")).thenReturn(present("0"));
        when(source.getValue("propertyConfiguration.list")).thenReturn(present("[1,2,3]"));
        when(source.getValue("propertyConfiguration.map")).thenReturn(present("{one:1,two:2}"));
        when(source.getValue("propertyConfiguration.mapOfLists")).thenReturn(present("{one:[1,11],two:[2,22]}"));

        PropertyConfiguration propertyConfiguration = factory.createConfiguration(PropertyConfiguration.class, source);

        assertThat(propertyConfiguration.getProperty()).isEqualTo(0);
        assertThat(propertyConfiguration.getList()).containsSequence(1, 2, 3);
        assertThat(propertyConfiguration.getMap()).containsExactly(entry("one", 1), entry("two", 2));
        assertThat(propertyConfiguration.getMapOfLists()).containsExactly(entry("one", asList(1, 11)), entry("two", asList(2, 22)));
        assertThat(propertyConfiguration.getComplexProperty()).hasSize(1);
        List<Map<List<String>, Map<List<String>, Map<String, List<Integer>>>>> expectedComplexProperty =
                singletonList(singletonMap(asList("a", "b"), singletonMap(asList("c", "d"), singletonMap("e", asList(1, 2)))));
        assertThat(propertyConfiguration.getComplexProperty()).isEqualTo(expectedComplexProperty);
    }

    @AbstractConfiguration
    @Key
    public interface AbstractPropertyConfiguration<K, V> {
        @Key
        V getProperty();

        @Key
        List<V> getList();

        @Key
        Map<K, V> getMap();

        @Key
        Map<K, List<V>> getMapOfLists();

        @Key
        @DefaultValue("[{[a,b]:{[c,d]:{e:[1,2]}}}]")
        List<Map<List<K>, Map<List<K>, Map<K, List<V>>>>> getComplexProperty();
    }

    public interface PropertyConfiguration extends AbstractPropertyConfiguration<String, Integer> {
    }

}


