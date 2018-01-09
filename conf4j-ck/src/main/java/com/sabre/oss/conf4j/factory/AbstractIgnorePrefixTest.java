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

import com.sabre.oss.conf4j.annotation.IgnoreKey;
import com.sabre.oss.conf4j.annotation.IgnorePrefix;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public abstract class AbstractIgnorePrefixTest<F extends AbstractConfigurationFactory> extends AbstractBaseConfigurationFactoryTest<F> {
    @Test
    public void shouldResetPrefixes() {
        when(source.getValue(anyString(), any())).thenReturn(absent());

        when(source.getValue("root.connection.url", null)).thenReturn(present("url"));
        // plain properties with @IgnorePrefix
        when(source.getValue("urlWithoutPrefix", null)).thenReturn(present("withoutPrefix"));
        // sub-configuration getTimeout()
        when(source.getValue("timeout.timeouts.connect", null)).thenReturn(present("2"));
        when(source.getValue("timeout.timeouts.read", null)).thenReturn(present("3"));
        // getTimeouts()
        when(source.getValue("size", null)).thenReturn(present("3"));
        when(source.getValue("[0].timeouts.connect", null)).thenReturn(present("100"));
        when(source.getValue("[0].timeouts.read", null)).thenReturn(present("110"));
        when(source.getValue("[1].timeouts.connect", null)).thenReturn(present("200"));
        when(source.getValue("[1].timeouts.read", null)).thenReturn(present("210"));
        // getTimeoutsWithPrefix()
        when(source.getValue("withPrefix.size", null)).thenReturn(present("3"));
        when(source.getValue("withPrefix[0].timeouts.connect", null)).thenReturn(present("1000"));
        when(source.getValue("withPrefix[0].timeouts.read", null)).thenReturn(present("1100"));
        when(source.getValue("withPrefix[1].timeouts.connect", null)).thenReturn(present("2000"));
        when(source.getValue("withPrefix[1].timeouts.read", null)).thenReturn(present("2100"));
        // when value for the third element is missing, it will be derived from fallback key for list which is a key without []
        when(source.getValue("withPrefix[2].timeouts.connect", null)).thenReturn(absent());
        when(source.getValue("withPrefix[2].timeouts.read", null)).thenReturn(absent());
        when(source.getValue("withPrefix.timeouts.connect", null)).thenReturn(present("99"));
        when(source.getValue("withPrefix.timeouts.read", null)).thenReturn(present("999"));

        RootConfiguration config = factory.createConfiguration(RootConfiguration.class, source);

        assertThat(config.getConnection().getUrl()).isEqualTo("url");
        assertThat(config.getConnection().getUrlWithoutPrefix()).isEqualTo("withoutPrefix");
        assertThat(config.getConnection().getTimeout().getConnectTimeout()).isEqualTo(2);
        assertThat(config.getConnection().getTimeout().getReadTimeout()).isEqualTo(3);

        List<TimeoutConfiguration> timeouts = config.getConnection().getTimeouts();
        assertThat(timeouts).hasSize(3);
        assertThat(timeouts.get(0).getConnectTimeout()).isEqualTo(100);
        assertThat(timeouts.get(0).getReadTimeout()).isEqualTo(110);
        assertThat(timeouts.get(1).getConnectTimeout()).isEqualTo(200);
        assertThat(timeouts.get(1).getReadTimeout()).isEqualTo(210);

        List<TimeoutConfiguration> timeoutsWithPrefix = config.getConnection().getTimeoutsWithPrefix();
        assertThat(timeoutsWithPrefix).hasSize(3);
        assertThat(timeoutsWithPrefix.get(0).getConnectTimeout()).isEqualTo(1000);
        assertThat(timeoutsWithPrefix.get(0).getReadTimeout()).isEqualTo(1100);
        assertThat(timeoutsWithPrefix.get(1).getConnectTimeout()).isEqualTo(2000);
        assertThat(timeoutsWithPrefix.get(1).getReadTimeout()).isEqualTo(2100);
        assertThat(timeoutsWithPrefix.get(2).getConnectTimeout()).isEqualTo(99);
        assertThat(timeoutsWithPrefix.get(2).getReadTimeout()).isEqualTo(999);

    }


    @Key("root")
    public interface RootConfiguration {
        @IgnoreKey
        ConnectionConfiguration getConnection();
    }

    @Key("connection")
    public interface ConnectionConfiguration {
        @Key
        String getUrl();

        @Key
        @IgnorePrefix
        String getUrlWithoutPrefix();

        @IgnorePrefix
        @Key
        TimeoutConfiguration getTimeout();

        @IgnorePrefix
        @IgnoreKey
        List<TimeoutConfiguration> getTimeouts();

        @IgnorePrefix
        @Key("withPrefix")
        List<TimeoutConfiguration> getTimeoutsWithPrefix();
    }

    @Key("timeouts")
    public interface TimeoutConfiguration {
        @Key("connect")
        int getConnectTimeout();

        @Key("read")
        int getReadTimeout();
    }
}


