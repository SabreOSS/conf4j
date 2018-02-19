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

package com.sabre.oss.conf4j.factory;

import com.sabre.oss.conf4j.annotation.Default;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.factory.jdkproxy.JdkProxyDynamicConfigurationFactory;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.internal.model.provider.annotation.AnnotationConfigurationModelProvider;
import com.sabre.oss.conf4j.internal.model.provider.convention.ConventionConfigurationModelProvider;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import com.sabre.oss.conf4j.source.TestConfigurationSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ConventionConfigurationFactoryTest {
    private final ConfigurationSource source = Mockito.spy(TestConfigurationSource.class);
    private AbstractConfigurationFactory configurationFactory;

    @Test
    public void shouldGiveSameResultsForAnnotationAndConventionBasedModelProviders() {
        configurationFactory = new JdkProxyDynamicConfigurationFactory();
        configurationFactory.setConfigurationModelProvider(AnnotationConfigurationModelProvider.getInstance());
        validate();

        configurationFactory = new JdkProxyDynamicConfigurationFactory();
        configurationFactory.setConfigurationModelProvider(ConventionConfigurationModelProvider.getInstance());
        validate();
    }

    private void validate() {
        // given
        when(source.getValue("url", null)).thenReturn(present("http://some.url"));
        when(source.getValue("userName", null)).thenReturn(present("user"));
        when(source.getValue("timeout.connectTimeout", null)).thenReturn(present("1000"));
        when(source.getValue("timeout.readTimeout", null)).thenReturn(present("2000"));
        when(source.getValue("otherTimeouts.size", null)).thenReturn(present("3"));
        when(source.getValue("otherTimeouts[0].connectTimeout", null)).thenReturn(present("100"));
        when(source.getValue("otherTimeouts[0].readTimeout", null)).thenReturn(present("200"));
        when(source.getValue("otherTimeouts[1].connectTimeout", null)).thenReturn(present("101"));
        when(source.getValue("otherTimeouts[1].readTimeout", null)).thenReturn(present("201"));
        when(source.getValue("otherTimeouts[2].connectTimeout", null)).thenReturn(present("0"));
        when(source.getValue("otherTimeouts[2].readTimeout", null)).thenReturn(present("0"));

        // when
        ConnectionConfiguration configuration = configurationFactory.createConfiguration(ConnectionConfiguration.class, source);

        // then
        assertThat(configuration.getUrl()).isEqualTo("http://some.url");
        assertThat(configuration.getUserName()).isEqualTo("user");
        assertThat(configuration.getTimeout().getConnectTimeout()).isEqualTo(1000);
        assertThat(configuration.getTimeout().getReadTimeout()).isEqualTo(2000);
        assertThat(configuration.getOtherTimeouts().size()).isEqualTo(3);

        assertThat(configuration.getOtherTimeouts().get(0).getConnectTimeout()).isEqualTo(100);
        assertThat(configuration.getOtherTimeouts().get(0).getReadTimeout()).isEqualTo(200);
        assertThat(configuration.getOtherTimeouts().get(1).getConnectTimeout()).isEqualTo(101);
        assertThat(configuration.getOtherTimeouts().get(1).getReadTimeout()).isEqualTo(201);
        assertThat(configuration.getOtherTimeouts().get(2).getConnectTimeout()).isEqualTo(0);
        assertThat(configuration.getOtherTimeouts().get(2).getReadTimeout()).isEqualTo(0);
    }

    public interface ConnectionConfiguration {
        @Key
        String getUrl();

        @Key
        String getUserName();

        TimeoutConfiguration getTimeout();

        List<TimeoutConfiguration> getOtherTimeouts();
    }

    public interface TimeoutConfiguration {
        @Key
        @Default("0")
        int getConnectTimeout();

        @Key
        @Default("0")
        int getReadTimeout();
    }
}


