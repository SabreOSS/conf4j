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

package com.sabre.oss.conf4j.factory.javassist;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.factory.AbstractDynamicConfigurationFactoryTest;
import com.sabre.oss.conf4j.internal.config.PropertyMetadata;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static com.sabre.oss.conf4j.internal.Constants.METADATA_SUFFIX;
import static org.assertj.core.api.Assertions.assertThat;

public class JavassistDynamicConfigurationFactoryTest extends AbstractDynamicConfigurationFactoryTest<JavassistDynamicConfigurationFactory> {
    @Override
    protected JavassistDynamicConfigurationFactory createConfigurationFactory() {
        return new JavassistDynamicConfigurationFactory();
    }

    @Test
    public void shouldInitializeDefaultValueAndKeyAndValuesConverter() {
        // when
        Configuration config = factory.createConfiguration(Configuration.class, source);
        // then
        assertThat(config.getSomeProperty()).isEqualTo("defaultValue");
        PropertyMetadata metaData = getFieldValue(config, "someProperty" + METADATA_SUFFIX);
        assertThat(metaData.getKeySet()).containsSequence("keyPrefix.someProperty");
        TypeConverter<?> typeConverter = this.<TypeConverter<?>>getFieldValue(config, "typeConverter");
        assertThat(typeConverter).isNotNull();
    }

    @Test
    public void shouldCreateProperClassStructure() {
        // when
        Configuration config = factory.createConfiguration(Configuration.class, source);
        // then
        assertThat(isFieldDeclaredOnClassDirectly(config, "someProperty" + METADATA_SUFFIX)).isTrue();
        assertThat(isFieldDeclaredOnClassDirectly(config, "someProperty")).isTrue();
        assertThat(isFieldDeclaredOnClassDirectly(config, "configurationSource")).isTrue();
        assertThat(isFieldDeclaredOnClassDirectly(config, "typeConverter")).isTrue();
        assertThat(isMethodDeclared(config, "getSomeProperty")).isTrue();
        assertThat(isMethodDeclared(config, "setSomeProperty" + METADATA_SUFFIX, PropertyMetadata.class)).isTrue();
        assertThat(isMethodDeclared(config, "setConfigurationSource", ConfigurationSource.class)).isTrue();
        assertThat(isMethodDeclared(config, "setTypeConverter", TypeConverter.class)).isTrue();
    }

    @Test
    public void shouldCreateFieldsForCompositeConfig() {
        // when
        CompositeConfiguration config = factory.createConfiguration(CompositeConfiguration.class, source);
        // then
        assertThat(isFieldDeclaredOnClassDirectly(config, "subConfiguration" + METADATA_SUFFIX)).isFalse();
        assertThat(isFieldDeclaredOnClassDirectly(config, "subConfiguration")).isTrue();
        assertThat(isFieldDeclaredOnClassDirectly(config, "configurationSource")).isTrue();
        assertThat(isFieldDeclaredOnClassDirectly(config.getSubConfiguration(), "someProperty")).isTrue();
        assertThat(isFieldDeclaredOnClassDirectly(config.getSubConfiguration(), "someProperty" + METADATA_SUFFIX)).isTrue();
    }

    private <T> T getFieldValue(Object object, String fieldName) {
        try {
            Field declaredField = object.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            @SuppressWarnings("unchecked")
            T value = (T) declaredField.get(object);
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <C> boolean isFieldDeclaredOnClassDirectly(C config, String name) {
        try {
            config.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return false;
        }
        return true;
    }

    private <T> boolean isMethodDeclared(T config, String name, Class<?>... parameterTypes) {
        try {
            config.getClass().getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }
}
