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

import com.sabre.oss.conf4j.annotation.*;
import com.sabre.oss.conf4j.converter.standard.BooleanTypeConverter;
import com.sabre.oss.conf4j.converter.standard.EscapingStringTypeConverter;
import com.sabre.oss.conf4j.converter.standard.IntegerTypeConverter;
import com.sabre.oss.conf4j.factory.model.ConfigurationWithIncompatibleAbstractMethod;
import com.sabre.oss.conf4j.factory.model.ConfigurationWithNoDefaultValue;
import com.sabre.oss.conf4j.factory.model.ValidConfiguration;
import com.sabre.oss.conf4j.factory.model.ValidConfiguration.SimpleEnum;
import com.sabre.oss.conf4j.factory.model.collections.Component;
import com.sabre.oss.conf4j.factory.model.hierarchical.FirstLevel;
import com.sabre.oss.conf4j.factory.model.ignoreprefix.BaseConfiguration;
import com.sabre.oss.conf4j.factory.model.keyprefix.ComponentsConfiguration;
import com.sabre.oss.conf4j.factory.model.keyprefix.ConfigurationWithoutKeyPrefixDefinition;
import com.sabre.oss.conf4j.factory.model.parameterized.ConfigurationWithIdSubConfiguration;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.processor.ConfigurationValueDecrypter;
import com.sabre.oss.conf4j.processor.ConfigurationValueDecryptingProcessor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public abstract class AbstractConfigurationFactoryTest<F extends AbstractConfigurationFactory> extends AbstractBaseConfigurationFactoryTest<F> {
    /**
     * Indicates whether abstract classes as configuration types are supported.
     *
     * @return {@code true} when abstract classes are supported
     */
    protected boolean supportsClasses() {
        return true;
    }

    @Test
    public void shouldThrowExceptionWhenClassContainsIncompatibleAbstractMethods() {
        // Expect
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(
                "public abstract java.lang.String com.sabre.oss.conf4j.factory.model.ConfigurationWithIncompatibleAbstractMethod.convertSomeValue(java.lang.String) " +
                        "is an abstract method but it is not a valid configuration property. Configuration property method must be abstract, public or protected, " +
                        "without parameters and its name starts with get or is (if return type is boolean). The return type cannot be void.");
        // When
        factory.createConfiguration(ConfigurationWithIncompatibleAbstractMethod.class, source);
    }

    @Test
    public void returnsDefaultValueIfValueNotFound() {
        // When
        when(source.getValue(anyString(), any())).thenReturn(absent());

        ValidConfiguration configInstance = factory.createConfiguration(ValidConfiguration.class, source);
        // Then
        assertThat(configInstance.getStringProperty()).isEqualTo("defaultValue");
    }

    @Test
    public void shouldReturnNullWhenLackOfValueAndDefaultIsNotDefined() {
        // When
        when(source.getValue(anyString(), any())).thenReturn(absent());
        ConfigurationWithNoDefaultValue configInstance = factory.createConfiguration(ConfigurationWithNoDefaultValue.class, source);
        assertThat(configInstance.getSampleValue()).isNull();
        assertThat(configInstance.getIntegerValue()).isNull();
    }

    @Test
    public void shouldAssignValueFromValuesSourceWhenAvailable() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("configuration.under.test.String.property", null)).thenReturn(present("string value"));
        when(source.getValue("configuration.under.test.Integer.property", null)).thenReturn(present("456"));
        // When
        ValidConfiguration configInstance = factory.createConfiguration(ValidConfiguration.class, source);
        // Then
        assertThat(configInstance.getIntegerProperty()).isEqualTo(456);
        assertThat(configInstance.getStringProperty()).isEqualTo("string value");
    }

    @Test
    public void shouldHandleAllStandardTypesOfProperties() {
        // When
        when(source.getValue(anyString(), any())).thenReturn(absent());
        ValidConfiguration configInstance = factory.createConfiguration(ValidConfiguration.class, source);

        // Then
        assertThat(configInstance).isInstanceOf(ValidConfiguration.class);
        assertThat(configInstance.getBooleanProperty()).isEqualTo(true);
        assertThat(configInstance.isSimpleBooleanProperty()).isEqualTo(true);
        assertThat(configInstance.getSimpleIntegerProperty()).isEqualTo(1);
        assertThat(configInstance.getIntegerProperty()).isEqualTo(-1);
        assertThat(configInstance.getSimpleLongProperty()).isEqualTo(1);
        assertThat(configInstance.getLongProperty()).isEqualTo(-1);
        assertThat(configInstance.getSimpleDoubleProperty()).isEqualTo(0.1);
        assertThat(configInstance.getDoubleProperty()).isEqualTo(-0.1);
        assertThat(configInstance.getStringProperty()).isEqualTo("defaultValue");
        assertThat(configInstance.getStringListProperty()).isEqualTo(asList("defaultValue1", "defaultValue2"));
        assertThat(configInstance.getEnumListProperty()).isEqualTo(asList(SimpleEnum.VALUE_1, SimpleEnum.VALUE_2));
        assertThat(configInstance.getStringListPropertyWithoutDefault()).isNull();
        assertThat(configInstance.getStringListPropertyWithNullDefault()).isNull();
        assertThat(configInstance.getStringListPropertyWithEmptyDefault()).isEmpty();
    }

    @Test
    public void shouldHandleHierarchicalConfigurations() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("L1.L2.L3.e", null)).thenReturn(present("TE1+"));
        when(source.getValue("L1.secondLevelConfig.L2.d", null)).thenReturn(present("SD0+"));

        // When
        FirstLevel firstLevel = factory.createConfiguration(FirstLevel.class, source);
        // Then
        assertThat(firstLevel.getA()).isEqualTo("FA1");
        assertThat(firstLevel.getB()).isEqualTo("FB1");
        assertThat(firstLevel.getC()).isEqualTo("FC0");
        assertThat(firstLevel.getSecondLevel().getC()).isEqualTo("SC1");
        assertThat(firstLevel.getSecondLevel().getD()).isEqualTo("SD0");
        assertThat(firstLevel.getPrefixedSecondLevel().getC()).isEqualTo("SC1");
        assertThat(firstLevel.getPrefixedSecondLevel().getD()).isEqualTo("SD0+");
        assertThat(firstLevel.getSecondLevel().getThirdLevel().getE()).isEqualTo("TE1+");
        assertThat(firstLevel.getSecondLevel().getThirdLevel().getF()).isEqualTo("TF0");
        assertThat(firstLevel.getSecondLevel().getThirdLevelByAnnotation1().getE()).isEqualTo("TE1");
        assertThat(firstLevel.getSecondLevel().getThirdLevelByAnnotation1().getF()).isEqualTo("TFAnnotation1");
        assertThat(firstLevel.getSecondLevel().getThirdLevelByAnnotation2().getE()).isEqualTo("TE1");
        assertThat(firstLevel.getSecondLevel().getThirdLevelByAnnotation2().getF()).isEqualTo("TFAnnotation2");
    }

    @Test
    public void shouldHandleMultipleKeys() {
        // Given
        when(source.getValue("configuration.int", null)).thenReturn(absent());
        when(source.getValue("configuration.intProperty", null)).thenReturn(present("100"));
        // When
        MultipleKeysConfiguration configuration = factory.createConfiguration(MultipleKeysConfiguration.class, source);
        // Then
        assertThat(configuration.getIntProperty()).isEqualTo(100);
        verify(source, times(1)).getValue("configuration.int", null);
        verify(source, times(1)).getValue("configuration.intProperty", null);
    }

    @Key("configuration")
    public interface MultipleKeysConfiguration {
        @Key({"int", "intProperty"})
        Integer getIntProperty();
    }

    @Test
    public void shouldHandleFallbackConfigurationForSubConfigurations() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("fallback.key.e", null)).thenReturn(present("fallbackValue"));
        // When
        FirstLevel configInstance = factory.createConfiguration(FirstLevel.class, source);
        // Then
        assertThat(configInstance.getSecondLevel().getThirdLevel().getE()).isEqualTo("fallbackValue");
    }

    @Test
    public void shouldApplyFallbackConfigurationAfterStandardForSubConfigurations() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("L1.L2.L3.e", null)).thenReturn(present("propertyValue"));
        when(source.getValue("fallback.key.e", null)).thenReturn(present("fallbackValue"));

        // When
        FirstLevel configInstance = factory.createConfiguration(FirstLevel.class, source);
        // Then
        assertThat(configInstance.getSecondLevel().getThirdLevel().getE()).isEqualTo("propertyValue");
    }

    @Test
    public void shouldHandleFallbackConfigurationForStandardProperties() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("configuration.under.test.property", null)).thenReturn(absent());
        when(source.getValue("fallback.property", null)).thenReturn(present("fallbackValue"));
        // When
        ValidConfiguration configInstance = factory.createConfiguration(ValidConfiguration.class, source);
        // Then
        assertThat(configInstance.getGlobalFallbackProperty()).isEqualTo("fallbackValue");
    }

    @Test
    public void shouldApplyFallbackConfigurationAfterStandardForStandardProperties() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("configuration.under.test.property", null)).thenReturn(present("propertyValue"));
        when(source.getValue("fallback.property", null)).thenReturn(present("follbackValue"));
        // When
        ValidConfiguration configInstance = factory.createConfiguration(ValidConfiguration.class, source);
        // Then
        assertThat(configInstance.getGlobalFallbackProperty()).isEqualTo("propertyValue");
    }

    @Test
    public void shouldHandleListOfSubConfigurations() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("component.subComponent.size", null)).thenReturn(present("3"));
        when(source.getValue("component.subComponent[0].propertyA", null)).thenReturn(present("A0+"));
        when(source.getValue("component.subComponent[0].propertyB", null)).thenReturn(present("B0+"));
        when(source.getValue("component.subComponent[1].propertyA", null)).thenReturn(present("A1+"));
        when(source.getValue("component.subComponent[2].propertyA", null)).thenReturn(present("A2+"));
        // When
        Component configInstance = factory.createConfiguration(Component.class, source);
        // Then
        assertThat(configInstance.getName()).isEqualTo("defaultName");
        assertThat(configInstance.getDescription()).isNull();
        assertThat(configInstance.getSubComponents()).hasSize(3);
        assertThat(configInstance.getSubComponents().get(0).getPropertyA()).isEqualTo("A0+"); // value from the source
        assertThat(configInstance.getSubComponents().get(0).getPropertyB()).isEqualTo("B0+"); // value from the source
        assertThat(configInstance.getSubComponents().get(1).getPropertyA()).isEqualTo("A1+"); // value from the source
        assertThat(configInstance.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");  // default from the list
        assertThat(configInstance.getSubComponents().get(2).getPropertyA()).isEqualTo("A2+"); // value from source
        assertThat(configInstance.getSubComponents().get(2).getPropertyB()).isEqualTo("B");   // default for property
    }

    @Test
    public void shouldHandleListOfSubConfigurationsWithAppendedItems() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("component.subComponent.size", null)).thenReturn(present("3"));
        when(source.getValue("component.subComponent[2].propertyA", null)).thenReturn(present("A2+"));
        // When
        Component configInstance = factory.createConfiguration(Component.class, source);
        // Then
        assertThat(configInstance.getName()).isEqualTo("defaultName");
        assertThat(configInstance.getDescription()).isNull();
        assertThat(configInstance.getSubComponents()).hasSize(3);
        assertThat(configInstance.getSubComponents().get(0).getPropertyA()).isEqualTo("A0");  // default from the list
        assertThat(configInstance.getSubComponents().get(0).getPropertyB()).isEqualTo("B0");  // default from the list
        assertThat(configInstance.getSubComponents().get(1).getPropertyA()).isEqualTo("A");   // default for property
        assertThat(configInstance.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");  // default from the list
        assertThat(configInstance.getSubComponents().get(2).getPropertyA()).isEqualTo("A2+"); // value from source
        assertThat(configInstance.getSubComponents().get(2).getPropertyB()).isEqualTo("B");   // default for property
    }

    @Test
    public void shouldHandleListOfSubConfigurationsWhenSizeIsMissing() {
        // Given component.subComponent.size=2 as a default from list annotation
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("component.subComponent[0].propertyA", null)).thenReturn(present("A0+"));
        when(source.getValue("component.subComponent[0].propertyB", null)).thenReturn(present("B0+"));
        when(source.getValue("component.subComponent[1].propertyA", null)).thenReturn(present("A1+"));
        when(source.getValue("component.subComponent[2].propertyA", null)).thenReturn(present("A2+")); // this value will be ignored
        // When
        Component configInstance = factory.createConfiguration(Component.class, source);
        // Then
        assertThat(configInstance.getName()).isEqualTo("defaultName");
        assertThat(configInstance.getDescription()).isNull();
        assertThat(configInstance.getSubComponents()).hasSize(2);
        assertThat(configInstance.getSubComponents().get(0).getPropertyA()).isEqualTo("A0+"); // value from source
        assertThat(configInstance.getSubComponents().get(0).getPropertyB()).isEqualTo("B0+"); // value from source
        assertThat(configInstance.getSubComponents().get(1).getPropertyA()).isEqualTo("A1+"); // value from source
        assertThat(configInstance.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");  // default from the list
    }

    @Test
    public void shouldHandleSubConfigurationWithMultipleKeyPrefixesUsedToProvideDefaultValues() {
        // Given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("prefix.default.component.name", null)).thenReturn(present("N+"));
        when(source.getValue("prefix.default.component.description", null)).thenReturn(present("D+"));
        when(source.getValue("prefix.default.component.subComponent.size", null)).thenReturn(present("3"));
        when(source.getValue("prefix.default.component.subComponent[0].propertyA", null)).thenReturn(present("A0+"));
        when(source.getValue("prefix.default.component.subComponent[1].propertyA", null)).thenReturn(absent());
        when(source.getValue("prefix.default.component.subComponent[2].propertyA", null)).thenReturn(present("A2+"));
        when(source.getValue("prefix.default.component.subComponent[2].propertyB", null)).thenReturn(present("B2+"));

        when(source.getValue("prefix.overridden.component.name", null)).thenReturn(present("N-"));
        when(source.getValue("prefix.overridden.component.description", null)).thenReturn(present("D-"));
        when(source.getValue("prefix.overridden.component.subComponent.size", null)).thenReturn(present("4"));
        when(source.getValue("prefix.overridden.component.subComponent[2].propertyB", null)).thenReturn(present("B2-"));
        when(source.getValue("prefix.overridden.component.subComponent[3].propertyA", null)).thenReturn(present("A3-"));

        when(source.getValue("prefix.overriddenList.size", null)).thenReturn(present("2"));
        when(source.getValue("prefix.overriddenList[1].component.subComponent[2].propertyB", null)).thenReturn(present("B2+-"));

        // When
        ComponentsConfiguration configInstance = factory.createConfiguration(ComponentsConfiguration.class, source);

        // Then
        Component defaultComponent = configInstance.getDefaultComponent();
        assertThat(defaultComponent.getName()).isEqualTo("N+");                                // value from source (prefix: overridden)
        assertThat(defaultComponent.getDescription()).isEqualTo("D+");                         // value from source (prefix: default)
        assertThat(defaultComponent.getSubComponents().size()).isEqualTo(3);
        assertThat(defaultComponent.getSubComponents().get(0).getPropertyA()).isEqualTo("A0+"); // value from source
        assertThat(defaultComponent.getSubComponents().get(0).getPropertyB()).isEqualTo("B0");  // value from source
        assertThat(defaultComponent.getSubComponents().get(1).getPropertyA()).isEqualTo("A");   // default for property
        assertThat(defaultComponent.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");  // default from the list
        assertThat(defaultComponent.getSubComponents().get(2).getPropertyA()).isEqualTo("A2+"); // value from source
        assertThat(defaultComponent.getSubComponents().get(2).getPropertyB()).isEqualTo("B2+"); // value from source

        Component overriddenComponent = configInstance.getOverriddenComponent();
        assertThat(overriddenComponent.getName()).isEqualTo("N-");                               // value from source (prefix: overridden)
        assertThat(overriddenComponent.getDescription()).isEqualTo("D-");                        // value from source (prefix: overridden)
        assertThat(overriddenComponent.getSubComponents().size()).isEqualTo(4);
        assertThat(overriddenComponent.getSubComponents().get(0).getPropertyA()).isEqualTo("A0+"); // value from source (prefix: default)
        assertThat(overriddenComponent.getSubComponents().get(0).getPropertyB()).isEqualTo("B0");  // value from source (prefix: default)
        assertThat(overriddenComponent.getSubComponents().get(1).getPropertyA()).isEqualTo("A");   // default for property
        assertThat(overriddenComponent.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");  // default from the list
        assertThat(overriddenComponent.getSubComponents().get(2).getPropertyA()).isEqualTo("A2+"); // value from source (prefix: default)
        assertThat(overriddenComponent.getSubComponents().get(2).getPropertyB()).isEqualTo("B2-"); // value from source (prefix: overridden)
        assertThat(overriddenComponent.getSubComponents().get(3).getPropertyA()).isEqualTo("A3-"); // value from source (prefix: overridden)
        assertThat(overriddenComponent.getSubComponents().get(3).getPropertyB()).isEqualTo("B");   // default for property

        List<Component> overriddenListOfComponents = configInstance.getOverriddenListOfComponents();
        assertThat(overriddenListOfComponents.size()).isEqualTo(2);

        Component giveConfigurationA = overriddenListOfComponents.get(0); // the same as default component
        assertThat(giveConfigurationA.getSubComponents().size()).isEqualTo(3);
        assertThat(giveConfigurationA.getSubComponents().get(0).getPropertyA()).isEqualTo("A0+"); // value from source
        assertThat(giveConfigurationA.getSubComponents().get(0).getPropertyB()).isEqualTo("B0");  // value from source
        assertThat(giveConfigurationA.getSubComponents().get(1).getPropertyA()).isEqualTo("A");   // default for property
        assertThat(giveConfigurationA.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");  // default from the list
        assertThat(giveConfigurationA.getSubComponents().get(2).getPropertyA()).isEqualTo("A2+"); // value from source
        assertThat(giveConfigurationA.getSubComponents().get(2).getPropertyB()).isEqualTo("B2+"); // value from source

        Component giveConfigurationB = overriddenListOfComponents.get(1); // a simple change in component to the default component
        assertThat(giveConfigurationB.getSubComponents().size()).isEqualTo(3);
        assertThat(giveConfigurationB.getSubComponents().get(0).getPropertyA()).isEqualTo("A0+"); // value from source
        assertThat(giveConfigurationB.getSubComponents().get(0).getPropertyB()).isEqualTo("B0");  // value from source
        assertThat(giveConfigurationB.getSubComponents().get(1).getPropertyA()).isEqualTo("A");   // default for property
        assertThat(giveConfigurationB.getSubComponents().get(1).getPropertyB()).isEqualTo("B1");  // default from the list
        assertThat(giveConfigurationB.getSubComponents().get(2).getPropertyA()).isEqualTo("A2+"); // value from source
        assertThat(giveConfigurationB.getSubComponents().get(2).getPropertyB()).isEqualTo("B2+-"); // value from source - the difference
    }

    @Test
    public void shouldHandleMoreThenOneAttributeForCollectionElement() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("component.subComponent.size", null)).thenReturn(present("1"));
        when(source.getValue("component.subComponent[0].propertyA", null)).thenReturn(present("A"));
        when(source.getValue("component.subComponent[0].propertyB", null)).thenReturn(present("B"));
        // when
        Component configInstance = factory.createConfiguration(Component.class, source);
        // then
        assertThat(configInstance.getSubComponents().size()).isEqualTo(1);
        assertThat(configInstance.getSubComponents().get(0).getPropertyA()).isEqualTo("A");
        assertThat(configInstance.getSubComponents().get(0).getPropertyB()).isEqualTo("B");
    }

    @Test
    public void shouldDetachConfigurationFromTheHierarchyWhenIgnoreKeyPrefixAnnotationIsUsed() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("baseConfigurationClass.subConfigurationProperty.subConfigurationClass.propertyA", null)).thenReturn(present("1")); // fully qualified key
        when(source.getValue("subSubConfigurationProperty.subSubConfigurationClass.propertyA", null)).thenReturn(present("2"));
        when(source.getValue("fallback.key.propertyB", null)).thenReturn(present("3"));
        when(source.getValue("subSubConfigurationListProperty.size", null)).thenReturn(present("4"));
        when(source.getValue("subSubConfigurationListProperty[0].subSubConfigurationClass.propertyA", null)).thenReturn(present("5"));
        when(source.getValue("subSubConfigurationListProperty[0].subSubConfigurationClass.propertyB", null)).thenReturn(present("6"));
        // when
        BaseConfiguration baseConfiguration = factory.createConfiguration(BaseConfiguration.class, source);
        // then
        assertThat(baseConfiguration.getSubConfiguration().getPropertyA()).isEqualTo(1);
        assertThat(baseConfiguration.getSubConfiguration().getSubSubConfiguration().getPropertyA()).isEqualTo(2); // enforced by @IgnorePrefix
        assertThat(baseConfiguration.getSubConfiguration().getSubSubConfiguration().getPropertyB()).isEqualTo(3); // fallback, global key
        assertThat(baseConfiguration.getSubConfiguration().getSubSubConfiguration().getPropertyA()).isEqualTo(2); // enforced by @IgnorePrefix
        assertThat(baseConfiguration.getSubConfiguration().getSubSubConfigurationList().size()).isEqualTo(4);     //
        assertThat(baseConfiguration.getSubConfiguration().getSubSubConfigurationList().get(0).getPropertyA()).isEqualTo(5);
        assertThat(baseConfiguration.getSubConfiguration().getSubSubConfigurationList().get(0).getPropertyB()).isEqualTo(6);
    }

    @Test
    public void shouldHandleClassesWithoutKeyPrefixAnnotation() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(present("A"));
        when(source.getValue("a.b.c", null)).thenReturn(present("B"));
        // when
        ConfigurationWithoutKeyPrefixDefinition configInstance = factory.createConfiguration(ConfigurationWithoutKeyPrefixDefinition.class, source);
        // then
        assertThat(configInstance.getProperty()).isEqualTo("B");
    }

    @Test
    public void shouldSupportConcreteMethods() {
        if (!supportsClasses()) {
            return;
        }

        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("configuration.name", null)).thenReturn(present("Name"));

        // when
        Configuration configuration = factory.createConfiguration(Configuration.class, source);

        // then
        assertThat(configuration.getName()).isEqualTo("Name");
        assertThat(configuration.getLowerCaseName()).isEqualTo("name");
    }

    @Key
    public abstract static class Configuration {
        @Key
        public abstract String getName();

        public String getLowerCaseName() {
            return StringUtils.lowerCase(getName());
        }
    }


    @Test
    public void shouldHandleParameterizedConfiguration() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("configurationWithIdSubConfiguration.property", null)).thenReturn(present("customValue"));
        when(source.getValue("configurationWithIdSubConfiguration.configuration.id", null)).thenReturn(present("1"));
        when(source.getValue("configurationWithIdSubConfiguration.configurations.size", null)).thenReturn(present("2"));
        when(source.getValue("configurationWithIdSubConfiguration.configurations[0].id", null)).thenReturn(present("0"));
        when(source.getValue("configurationWithIdSubConfiguration.configurations[1].id", null)).thenReturn(present("1"));

        // when
        ConfigurationWithIdSubConfiguration configuration = factory.createConfiguration(ConfigurationWithIdSubConfiguration.class, source);

        // then
        assertThat(configuration.getProperty()).isEqualTo("customValue");
        assertThat(configuration.getConfiguration().getId()).isEqualTo(1);
        assertThat(configuration.getConfigurations()).hasSize(2);
        assertThat(configuration.getConfigurations().get(0).getId()).isEqualTo(0);
        assertThat(configuration.getConfigurations().get(1).getId()).isEqualTo(1);
    }

    @Test
    public void shouldSupportNestedPublicInterfaces() {
        // given
        when(source.getValue("connection.url", null)).thenReturn(present("http://primary"));
        when(source.getValue("connection.url2", null)).thenReturn(present("http://alternate"));
        when(source.getValue("connection.active", null)).thenReturn(present("true"));
        when(source.getValue("connection.mandatory", null)).thenReturn(present("true"));

        // when
        ConnectionConfiguration config = factory.createConfiguration(ConnectionConfiguration.class, source);

        // then
        assertThat(config.getUrl()).isEqualTo("http://primary");
        assertThat(config.getAlternateUrl()).isEqualTo("http://alternate");
        assertThat(config.isActive()).isEqualTo(true);
        assertThat(config.getMandatory()).isEqualTo(Boolean.TRUE);
    }

    @Key("connection")
    public interface ConnectionConfiguration {
        @Key
        String getUrl();

        @Key("url2")
        String getAlternateUrl();

        @Key
        boolean isActive();

        @Key
        Boolean getMandatory();
    }

    @Test
    public void shouldSupportEncryption() {
        // given
        ConfigurationValueDecrypter decrypter = mock(ConfigurationValueDecrypter.class);
        when(decrypter.getName()).thenReturn(Encrypted.DEFAULT);
        when(decrypter.decrypt(anyString())).thenReturn("decrypted");

        factory.setConfigurationValueProcessors(singletonList(
                new ConfigurationValueDecryptingProcessor(decrypter)
        ));

        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("connection.url", null)).thenReturn(present("http://primary"));
        when(source.getValue("connection.password", null)).thenReturn(present("encrypted"));


        // when
        EncryptedConfiguration config = factory.createConfiguration(EncryptedConfiguration.class, source);

        // then
        assertThat(config.getUrl()).isEqualTo("http://primary");
        assertThat(config.getPassword()).isEqualTo("decrypted");
    }

    @Key("connection")
    public interface EncryptedConfiguration {
        @Key
        String getUrl();

        @Key
        @Encrypted
        String getPassword();
    }

    @Test
    public void shouldUseCustomTypeConverter() {
        // given
        when(source.getValue("configuration.stringProperty", null)).thenReturn(present("string"));
        when(source.getValue("configuration.booleanProperty", null)).thenReturn(present("true"));
        // set converter which doesn't support String nor boolean
        factory.setTypeConverter(new IntegerTypeConverter());

        // when
        CustomConfiguration config = factory.createConfiguration(CustomConfiguration.class, source);

        // then
        assertThat(config.getStringProperty()).isEqualTo("string");
        assertThat(config.isBooleanProperty()).isEqualTo(true);
    }

    @Key("configuration")
    public interface CustomConfiguration {
        @Key
        @Converter(EscapingStringTypeConverter.class)
        String getStringProperty();

        @Key
        @Converter(BooleanTypeConverter.class)
        boolean isBooleanProperty();
    }

    @Test
    public void shouldPreventAbstractConfigurationInstantiation() {
        // given
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(format("Configuration type %s is marked as abstract configuration and creating an configuration instance is forbidden.", AnAbstractConfiguration.class.getName()));
        // when
        factory.createConfiguration(AnAbstractConfiguration.class, source);
        // then
    }

    @AbstractConfiguration
    public interface AnAbstractConfiguration {
        @Key("value")
        String getValue();
    }

    @Test
    public void shouldDetectCycles() {
        // given
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cycle between configurations detected");
        // when
        factory.createConfiguration(CycleConfiguration.class, source);
        // then
    }

    @Key
    public interface CycleConfiguration {
        CloseCycleConfiguration getCloseCycleConfiguration();
    }

    @Key
    public interface CloseCycleConfiguration {
        CycleConfiguration getCycleConfiguration();
    }

    @Test
    public void shouldImplementEqualsHashCodeEquals() {
        ConnectionConfiguration configuration = factory.createConfiguration(ConnectionConfiguration.class, source);
        ConnectionConfiguration otherConfiguration = factory.createConfiguration(ConnectionConfiguration.class, source);

        assertThat(configuration.equals(configuration)).isTrue();
        assertThat(configuration.equals(otherConfiguration)).isFalse();
        assertThat(configuration.equals(new Object())).isFalse();

        // validate hashCode is implemented
        configuration.hashCode();

        // validate toString is implemented
        configuration.toString();
    }

    @Test
    public void shouldHandlePropertiesWhichConflictWithReservedJavaLiterals() {
        factory.createConfiguration(ReservedLiteralsConfiguration.class, source);
    }

    public interface ReservedLiteralsConfiguration {
        @Key
        String getAbstract();

        @Key
        String getContinue();

        @Key
        String getFor();

        @Key
        String getNew();

        @Key
        String getSwitch();

        @Key
        String getAssert();

        @Key
        String getDefault();

        @Key
        String getIf();

        @Key
        String getPackage();

        @Key
        String getSynchronized();

        @Key
        String getBoolean();

        @Key
        String getDo();

        @Key
        String getGoto();

        @Key
        String getPrivate();

        @Key
        String getThis();

        @Key
        String getBreak();

        @Key
        String getDouble();

        @Key
        String getImplements();

        @Key
        String getProtected();

        @Key
        String getThrow();

        @Key
        String getByte();

        @Key
        String getElse();

        @Key
        String getImport();

        @Key
        String getPublic();

        @Key
        String getThrows();

        @Key
        String getCase();

        @Key
        String getEnum();

        @Key
        String getInstanceof();

        @Key
        String getReturn();

        @Key
        String getTransient();

        @Key
        String getCatch();

        @Key
        String getExtends();

        @Key
        String getInt();

        @Key
        String getShort();

        @Key
        String getTry();

        @Key
        String getChar();

        @Key
        String getFinal();

        @Key
        String getInterface();

        @Key
        String getStatic();

        @Key
        String getVoid();

        @Key
        String getFinally();

        @Key
        String getLong();

        @Key
        String getStrictfp();

        @Key
        String getVolatile();

        @Key
        String getConst();

        @Key
        String getFloat();

        @Key
        String getNative();

        @Key
        String getSuper();

        @Key
        String getWhile();

        @Key
        String getNull();

        @Key
        String getTrue();

        @Key
        String getFalse();

        @Key
        String get666StartWithNumber();

        SubConfig get666SubConfig();

        SubConfig get666SubList();

        interface SubConfig {
            @Key
            String get1();
        }
    }

    @Test
    public void shouldProvideCustomMetadata() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());

        Map<String, String> attributes = MapUtils.of("custom", "not-overridden", "file", "my.properties");
        Map<String, String> overriddenAttributes = MapUtils.of("custom", "not-overridden", "file", "overridden.properties");
        Map<String, String> anotherAttributes = MapUtils.of("custom", "not-overridden", "file", "another.properties");

        when(source.getValue("metadataConfiguration.value", attributes)).thenReturn(present("value"));
        when(source.getValue("metadataConfiguration.override", overriddenAttributes)).thenReturn(present("overridden"));

        when(source.getValue("metadataConfiguration.subConfiguration.value", attributes)).thenReturn(present("value"));
        when(source.getValue("metadataConfiguration.overrideSubConfiguration.value", overriddenAttributes)).thenReturn(present("overridden"));

        when(source.getValue("metadataConfiguration.subConfiguration.another", anotherAttributes)).thenReturn(present("overridden"));
        when(source.getValue("metadataConfiguration.overrideSubConfiguration.another", anotherAttributes)).thenReturn(present("overridden"));

        when(source.getValue("metadataConfiguration.subConfigurationList.size", attributes)).thenReturn(present("1"));
        when(source.getValue("metadataConfiguration.subConfigurationList[0].value", attributes)).thenReturn(present("value"));

        when(source.getValue("metadataConfiguration.overrideSubConfigurationList.size", overriddenAttributes)).thenReturn(present("2"));
        when(source.getValue("metadataConfiguration.overrideSubConfigurationList[0].value", overriddenAttributes)).thenReturn(present("overridden"));

        // when
        MetadataConfiguration configuration = factory.createConfiguration(MetadataConfiguration.class, source);

        // then
        assertThat(configuration.getValue()).isEqualTo("value");
        assertThat(configuration.getOverride()).isEqualTo("overridden");

        assertThat(configuration.getSubConfiguration().getValue()).isEqualTo("value");
        assertThat(configuration.getOverrideSubConfiguration().getValue()).isEqualTo("overridden");

        assertThat(configuration.getSubConfiguration().getAnother()).isEqualTo("overridden");
        assertThat(configuration.getOverrideSubConfiguration().getAnother()).isEqualTo("overridden");

        assertThat(configuration.getSubConfigurationList()).hasSize(1);
        assertThat(configuration.getSubConfigurationList().get(0).getValue()).isEqualTo("value");

        assertThat(configuration.getOverrideSubConfigurationList()).hasSize(2);
        assertThat(configuration.getOverrideSubConfigurationList().get(0).getValue()).isEqualTo("overridden");
    }

    @Meta(name = "file")
    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @Documented
    public @interface SourceFile {
        String value();
    }

    @Meta(name = "custom", value = "not-overridden")
    @SourceFile("my.properties")
    @Key
    public interface MetadataConfiguration {
        @Key
        String getValue();

        MetadataSubConfiguration getSubConfiguration();

        List<MetadataSubConfiguration> getSubConfigurationList();

        @Key
        @Meta(name = "file", value = "overridden.properties")
        String getOverride();

        @Meta(name = "file", value = "overridden.properties")
        MetadataSubConfiguration getOverrideSubConfiguration();

        @SourceFile("overridden.properties")
        List<MetadataSubConfiguration> getOverrideSubConfigurationList();
    }

    public interface MetadataSubConfiguration {
        @Key
        String getValue();

        @Key
        @Meta(name = "file", value = "another.properties")
        String getAnother();
    }
}
