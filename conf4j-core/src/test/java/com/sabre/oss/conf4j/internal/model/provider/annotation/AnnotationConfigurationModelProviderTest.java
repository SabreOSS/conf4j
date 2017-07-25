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

package com.sabre.oss.conf4j.internal.model.provider.annotation;

import com.sabre.oss.conf4j.annotation.*;
import com.sabre.oss.conf4j.converter.standard.EscapingStringTypeConverter;
import com.sabre.oss.conf4j.internal.model.*;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationConfigurationModelProviderTest {
    private final ConfigurationModelProvider provider = AnnotationConfigurationModelProvider.getInstance();

    @Test
    public void shouldParseConfigurationClass() {
        // given
        Class<ConnectionConfiguration> configurationType = ConnectionConfiguration.class;

        // when
        ConfigurationModel model = provider.getConfigurationModel(configurationType);

        // then
        assertThat(model).isNotNull();
        assertThat(model.getConfigurationType()).isSameAs(configurationType);
        assertThat(model.getDescription()).isEqualTo("connection configuration");
        assertThat(model.isAbstractConfiguration()).isFalse();
        assertThat(model.getPrefixes()).containsSequence("connection");
        assertThat(model.getProperties()).hasSize(3);

        ValuePropertyModel urlProperty = property(model, "url");
        assertThat(urlProperty.getType()).isEqualTo(String.class);
        assertThat(urlProperty.getMethod()).isNotNull();
        assertThat(urlProperty.getDescription()).isEqualTo("target url");
        assertThat(urlProperty.getEffectiveKey()).containsSequence("url");
        assertThat(urlProperty.getFallbackKey()).containsSequence("fallback.url");
        assertThat(urlProperty.isResetPrefix()).isFalse();
        assertThat(urlProperty.getEncryptionProviderName()).isEqualTo("secured");
        assertThat(urlProperty.getDefaultValue().get()).isEqualTo("http://url.com");
        assertThat(urlProperty.getTypeConverterClass()).isEqualTo(EscapingStringTypeConverter.class);

        SubConfigurationPropertyModel timeoutProperty = property(model, "timeout");
        assertThat(timeoutProperty.getType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(timeoutProperty.getMethod()).isNotNull();
        assertThat(timeoutProperty.getDescription()).isEqualTo("timeout settings");
        assertThat(timeoutProperty.getDeclaredType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(timeoutProperty.getTypeModel().getConfigurationType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(timeoutProperty.getFallbackKey()).isNull();
        assertThat(timeoutProperty.isResetPrefix()).isTrue();
        assertThat(timeoutProperty.getPrefixes()).containsSequence("timeout");
        assertThat(timeoutProperty.getDefaultValues()).isEmpty();


        SubConfigurationListPropertyModel allTimeoutsProperty = property(model, "allTimeouts");
        assertThat(allTimeoutsProperty.getType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(allTimeoutsProperty.getMethod()).isNotNull();
        assertThat(allTimeoutsProperty.getDescription()).isEqualTo("all timeouts");
        assertThat(allTimeoutsProperty.getItemTypeModel().getConfigurationType()).isEqualTo(TimeoutConfiguration.class);
        assertThat(allTimeoutsProperty.getDefaultSize()).isEqualTo(0);
        assertThat(allTimeoutsProperty.isResetPrefix()).isFalse();
        assertThat(allTimeoutsProperty.getPrefixes()).containsSequence("allTimeouts");
        assertThat(allTimeoutsProperty.getDefaultValues()).isEmpty();
    }

    private <T extends PropertyModel> T property(ConfigurationModel configurationModel, String property) {
        return configurationModel.getProperties().stream()
                .filter(p -> p.getPropertyName().equals(property))
                .map(p -> (T) p)
                .findAny().get();
    }

    @Key("connection")
    @Description("connection configuration")
    public interface ConnectionConfiguration {
        @Key("url")
        @FallbackKey("fallback.url")
        @DefaultValue("http://url.com")
        @Description("target url")
        @Encrypted("secured")
        @Converter(EscapingStringTypeConverter.class)
        String getUrl();

        @Key("timeout")
        @Description("timeout settings")
        @IgnorePrefix
        TimeoutConfiguration getTimeout();

        @Description("all timeouts")
        @Key("allTimeouts")
        List<TimeoutConfiguration> getAllTimeouts();
    }

    public interface TimeoutConfiguration {
        @Key("connect")
        int getConnectTimeout();

        @Key("read")
        int getReadTimeout();
    }
}
