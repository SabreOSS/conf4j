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

package com.sabre.oss.conf4j.internal.model.provider.convention;

import com.sabre.oss.conf4j.annotation.*;
import com.sabre.oss.conf4j.converter.StringConverter;
import com.sabre.oss.conf4j.internal.model.*;
import com.sabre.oss.conf4j.internal.model.provider.convention.ConventionConfigurationModelProviderTest.AnnotatedTimeoutConfiguration.DefaultTimeout;
import com.sabre.oss.conf4j.internal.utils.MapUtils;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConventionConfigurationModelProviderTest {
    private final ConfigurationModelProvider provider = ConventionConfigurationModelProvider.getInstance();

    @Test
    public void shouldGenerateModelBasedOnConvention() {
        // given
        Class<ConnectionConfiguration> configurationType = ConnectionConfiguration.class;

        // when
        ConfigurationModel model = provider.getConfigurationModel(configurationType);

        // then
        assertThat(model).isNotNull();
        assertThat(model.getConfigurationType()).isSameAs(configurationType);
        assertThat(model.getDescription()).isNull();
        assertThat(model.isAbstractConfiguration()).isFalse();
        assertThat(model.getPrefixes()).isEmpty();
        assertThat(model.getProperties()).hasSize(3);

        ValuePropertyModel urlProperty = property(model, "url");
        assertThat(urlProperty.getType()).isEqualTo(String.class);
        assertThat(urlProperty.getMethod()).isNotNull();
        assertThat(urlProperty.getDescription()).isNull();
        assertThat(urlProperty.getEffectiveKey()).containsSequence("url");
        assertThat(urlProperty.getFallbackKey()).isNull();
        assertThat(urlProperty.isResetPrefix()).isFalse();
        assertThat(urlProperty.getEncryptionProviderName()).isNull();
        assertThat(urlProperty.getDefaultValue().isPresent()).isFalse();
        assertThat(urlProperty.getTypeConverterClass()).isNull();

        SubConfigurationPropertyModel timeoutProperty = property(model, "timeout");
        assertThat(timeoutProperty.getType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(timeoutProperty.getMethod()).isNotNull();
        assertThat(timeoutProperty.getDescription()).isNull();
        assertThat(timeoutProperty.getDeclaredType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(timeoutProperty.getTypeModel().getConfigurationType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(timeoutProperty.getFallbackKey()).isNull();
        assertThat(timeoutProperty.isResetPrefix()).isFalse();
        assertThat(timeoutProperty.getPrefixes()).containsSequence("timeout");
        assertThat(timeoutProperty.getDefaultValues()).isEmpty();

        SubConfigurationListPropertyModel allTimeoutsProperty = property(model, "allTimeouts");
        assertThat(allTimeoutsProperty.getType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(allTimeoutsProperty.getMethod()).isNotNull();
        assertThat(allTimeoutsProperty.getDescription()).isNull();
        assertThat(allTimeoutsProperty.getItemTypeModel().getConfigurationType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(allTimeoutsProperty.getDefaultSize()).isEqualTo(0);
        assertThat(allTimeoutsProperty.isResetPrefix()).isFalse();
        assertThat(allTimeoutsProperty.getPrefixes()).containsSequence("allTimeouts");
        assertThat(allTimeoutsProperty.getDefaultValues()).isEmpty();
    }

    @Test
    public void shouldProvideDefaultValuesForPrimitives() {
        // given
        Class<TimeoutConfiguration> configurationType = TimeoutConfiguration.class;

        // when
        ConfigurationModel model = provider.getConfigurationModel(configurationType);

        assertThat(model).isNotNull();
        assertThat(model.getConfigurationType()).isSameAs(configurationType);
        assertThat(model.getDescription()).isNull();
        assertThat(model.isAbstractConfiguration()).isFalse();
        assertThat(model.getPrefixes()).isEmpty();
        assertThat(model.getProperties()).hasSize(2);

        ValuePropertyModel connectionTimeoutProperty = property(model, "connectTimeout");
        assertThat(connectionTimeoutProperty.getType()).isEqualTo(int.class);
        assertThat(connectionTimeoutProperty.getDefaultValue()).isEqualTo(present("0"));

        ValuePropertyModel readTimeoutProperty = property(model, "readTimeout");
        assertThat(readTimeoutProperty.getType()).isEqualTo(int.class);
        assertThat(readTimeoutProperty.getDefaultValue()).isEqualTo(present("0"));
    }

    public interface ConnectionConfiguration {
        String getUrl();

        TimeoutConfiguration getTimeout();

        List<TimeoutConfiguration> getAllTimeouts();
    }

    public interface TimeoutConfiguration {
        int getConnectTimeout();

        int getReadTimeout();
    }

    @Test
    public void shouldGenerateModelBasedOnConventionAndAnnotationsWhenAvailable() {
        // given
        Class<AnnotatedConnectionConfiguration> configurationType = AnnotatedConnectionConfiguration.class;

        // when
        ConfigurationModel model = provider.getConfigurationModel(configurationType);

        // then
        assertThat(model).isNotNull();
        assertThat(model.getConfigurationType()).isSameAs(configurationType);
        assertThat(model.getDescription()).isEqualTo("from annotation");
        assertThat(model.isAbstractConfiguration()).isFalse();
        assertThat(model.getPrefixes()).containsSequence("connection", "connection2");
        assertThat(model.getProperties()).hasSize(3);

        ValuePropertyModel urlProperty = property(model, "url");
        assertThat(urlProperty.getType()).isEqualTo(String.class);
        assertThat(urlProperty.getMethod()).isNotNull();
        assertThat(urlProperty.getDescription()).isEqualTo("url");
        assertThat(urlProperty.getEffectiveKey()).containsSequence("connectionUrl");
        assertThat(urlProperty.getFallbackKey()).containsSequence("fallbackConnectionUrl");
        assertThat(urlProperty.isResetPrefix()).isFalse();
        assertThat(urlProperty.getEncryptionProviderName()).isEqualTo("default");
        assertThat(urlProperty.getDefaultValue()).isEqualTo(present("http://sabre.com"));
        assertThat(urlProperty.getTypeConverterClass()).isEqualTo(StringConverter.class);

        SubConfigurationPropertyModel timeoutProperty = property(model, "timeout");
        assertThat(timeoutProperty.getType()).isEqualTo(AnnotatedTimeoutConfiguration.class);
        assertThat(timeoutProperty.getMethod()).isNotNull();
        assertThat(timeoutProperty.getDescription()).isNull();
        assertThat(timeoutProperty.getDeclaredType()).isEqualTo(AnnotatedTimeoutConfiguration.class);
        assertThat(timeoutProperty.getTypeModel().getConfigurationType()).isEqualTo(AnnotatedTimeoutConfiguration.class);
        assertThat(timeoutProperty.getFallbackKey()).isNull();
        assertThat(timeoutProperty.isResetPrefix()).isTrue();
        assertThat(timeoutProperty.getPrefixes()).containsSequence("timeout");
        assertThat(timeoutProperty.getDefaultValues()).contains(entry("connectTimeout", "100"), entry("readTimeout", "200"));

        SubConfigurationListPropertyModel allTimeoutsProperty = property(model, "allTimeouts");
        assertThat(allTimeoutsProperty.getType()).isEqualTo(AnnotatedTimeoutConfiguration.class);
        assertThat(allTimeoutsProperty.getMethod()).isNotNull();
        assertThat(allTimeoutsProperty.getDescription()).isNull();
        assertThat(allTimeoutsProperty.getItemTypeModel().getConfigurationType()).isEqualTo(AnnotatedTimeoutConfiguration.class);
        assertThat(allTimeoutsProperty.getDefaultSize()).isEqualTo(2);
        assertThat(allTimeoutsProperty.isResetPrefix()).isFalse();
        assertThat(allTimeoutsProperty.getPrefixes()).containsSequence("allTimeouts");
        assertThat(allTimeoutsProperty.getDefaultValues()).contains(
                MapUtils.of("connectTimeout", "100", "readTimeout", "1000"),
                MapUtils.of("connectTimeout", "200", "readTimeout", "2000")
        );
    }

    @Key({"connection", "connection2"})
    @Description("from annotation")
    public interface AnnotatedConnectionConfiguration {
        @Key("connectionUrl")
        @FallbackKey("fallbackConnectionUrl")
        @Default("http://sabre.com")
        @Description("url")
        @Encrypted("default")
        @Converter(StringConverter.class)
        String getUrl();

        @IgnorePrefix
        @DefaultTimeout(connectTimeout = "100", readTimeout = "200")
        AnnotatedTimeoutConfiguration getTimeout();

        @DefaultTimeout(connectTimeout = "100", readTimeout = "1000")
        @DefaultTimeout(connectTimeout = "200", readTimeout = "2000")
        List<AnnotatedTimeoutConfiguration> getAllTimeouts();
    }

    @DefaultsAnnotation(DefaultTimeout.class)
    public interface AnnotatedTimeoutConfiguration {
        @Default("100")
        int getConnectTimeout();

        @Default("200")
        int getReadTimeout();

        @Target(METHOD)
        @Retention(RUNTIME)
        @Repeatable(DefaultTimeouts.class)
        @Documented
        @interface DefaultTimeout {
            String connectTimeout();

            String readTimeout() default "3000";
        }

        @Target(METHOD)
        @Retention(RUNTIME)
        @Documented
        @interface DefaultTimeouts {
            DefaultTimeout[] value();
        }
    }

    @Test
    public void shouldDetectDisallowedAnnotations() {
        assertThrows(Exception.class, () -> {
            provider.getConfigurationModel(InvalidAnnotatedConfiguration.class);
        }, "InvalidAnnotatedConfiguration.getValue() method is annotated with disallowed annotation(s): @IgnoreKey.");
    }

    public interface InvalidAnnotatedConfiguration {
        @Key
        @IgnoreKey
        String getValue();
    }

    @SuppressWarnings("unchecked")
    private <T extends PropertyModel> T property(ConfigurationModel configurationModel, String property) {
        return configurationModel.getProperties().stream()
                .filter(p -> p.getPropertyName().equals(property))
                .map(p -> (T) p)
                .findAny().get();
    }
}
