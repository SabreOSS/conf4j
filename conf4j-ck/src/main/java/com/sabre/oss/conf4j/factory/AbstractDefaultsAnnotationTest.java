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

import com.sabre.oss.conf4j.annotation.Default;
import com.sabre.oss.conf4j.annotation.DefaultSize;
import com.sabre.oss.conf4j.annotation.DefaultsAnnotation;
import com.sabre.oss.conf4j.annotation.Key;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static com.sabre.oss.conf4j.source.OptionalValue.absent;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public abstract class AbstractDefaultsAnnotationTest<F extends AbstractConfigurationFactory> extends AbstractBaseConfigurationFactoryTest<F> {
    @Test
    public void shouldProvideDefaultValuesAndUseDefaultSize() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());

        // when
        ConfigWithoutPrefixes config = factory.createConfiguration(ConfigWithoutPrefixes.class, source);

        // then
        assertThat(config.getWithoutDefaultsAnnotation().getIntProperty()).isEqualTo(1);
        assertThat(config.getWithoutDefaultsAnnotation().getStringProperty()).isEqualTo("default");

        assertThat(config.getWithDefaultsAnnotation().getIntProperty()).isEqualTo(2);
        assertThat(config.getWithDefaultsAnnotation().getStringProperty()).isEqualTo("withDefaults");

        assertThat(config.getListWithoutDefaultsAnnotation().size()).isEqualTo(0);

        List<SubConfig> listWithDefaultsAnnotation = config.getListWithDefaultsAnnotation();
        assertThat(listWithDefaultsAnnotation.size()).isEqualTo(3); // from @DefaultSize
        assertThat(listWithDefaultsAnnotation.get(0).getIntProperty()).isEqualTo(1);
        assertThat(listWithDefaultsAnnotation.get(0).getStringProperty()).isEqualTo("withDefaultsList-1");
        assertThat(listWithDefaultsAnnotation.get(1).getIntProperty()).isEqualTo(2);
        assertThat(listWithDefaultsAnnotation.get(1).getStringProperty()).isNull();
        assertThat(listWithDefaultsAnnotation.get(2).getIntProperty()).isEqualTo(3);
        assertThat(listWithDefaultsAnnotation.get(2).getStringProperty()).isEqualTo("default");
    }

    @Test
    public void shouldProvideValuesFromValueSourceAndSizeIsLessThanNumberOfDefaults() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("config.withoutDefaultsAnnotation.int", null)).thenReturn(present("10"));
        when(source.getValue("config.withoutDefaultsAnnotation.string", null)).thenReturn(present("fromSource"));

        when(source.getValue("config.withDefaultsAnnotation.int", null)).thenReturn(present("10"));
        when(source.getValue("config.withDefaultsAnnotation.string", null)).thenReturn(present("fromSource"));

        when(source.getValue("config.listWithoutDefaultsAnnotation.size", null)).thenReturn(present("2"));
        when(source.getValue("config.listWithDefaultsAnnotation.size", null)).thenReturn(present("2"));
        when(source.getValue("config.listWithDefaultsAnnotation.int", null)).thenReturn(present("10"));
        when(source.getValue("config.listWithDefaultsAnnotation.string", null)).thenReturn(present("fromSource"));

        // when
        ConfigWithoutPrefixes config = factory.createConfiguration(ConfigWithoutPrefixes.class, source);

        // then
        assertThat(config.getWithoutDefaultsAnnotation().getIntProperty()).isEqualTo(10);
        assertThat(config.getWithoutDefaultsAnnotation().getStringProperty()).isEqualTo("fromSource");

        assertThat(config.getWithDefaultsAnnotation().getIntProperty()).isEqualTo(10);
        assertThat(config.getWithDefaultsAnnotation().getStringProperty()).isEqualTo("fromSource");

        assertThat(config.getListWithoutDefaultsAnnotation().size()).isEqualTo(2); // size is from value source

        List<SubConfig> listWithDefaultsAnnotation = config.getListWithDefaultsAnnotation();
        assertThat(listWithDefaultsAnnotation.size()).isEqualTo(2);
        assertThat(listWithDefaultsAnnotation.get(0).getIntProperty()).isEqualTo(10);
        assertThat(listWithDefaultsAnnotation.get(0).getStringProperty()).isEqualTo("fromSource");
        assertThat(listWithDefaultsAnnotation.get(1).getIntProperty()).isEqualTo(10);
        assertThat(listWithDefaultsAnnotation.get(1).getStringProperty()).isEqualTo("fromSource");
    }

    @Test
    public void shouldProvideValuesFromValueSourceAndSizeIsGreaterThanNumberOfDefaults() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("config.listWithoutDefaultsAnnotation.size", null)).thenReturn(present("5"));
        when(source.getValue("config.listWithoutDefaultsAnnotation[0].int", null)).thenReturn(present("11"));
        when(source.getValue("config.listWithoutDefaultsAnnotation[0].string", null)).thenReturn(present("fromSource-1"));
        when(source.getValue("config.listWithoutDefaultsAnnotation[1].int", null)).thenReturn(present("22"));
        when(source.getValue("config.listWithoutDefaultsAnnotation[1].string", null)).thenReturn(present("fromSource-2"));

        when(source.getValue("config.listWithDefaultsAnnotation.size", null)).thenReturn(present("5"));
        when(source.getValue("config.listWithDefaultsAnnotation[0].int", null)).thenReturn(present("11"));
        when(source.getValue("config.listWithDefaultsAnnotation[0].string", null)).thenReturn(present("fromSource-1"));
        when(source.getValue("config.listWithDefaultsAnnotation[1].int", null)).thenReturn(present("22"));
        when(source.getValue("config.listWithDefaultsAnnotation[1].string", null)).thenReturn(present("fromSource-2"));

        // when
        ConfigWithoutPrefixes config = factory.createConfiguration(ConfigWithoutPrefixes.class, source);

        // then
        assertThat(config.getWithoutDefaultsAnnotation().getIntProperty()).isEqualTo(1); // from SubConfig property default
        assertThat(config.getWithoutDefaultsAnnotation().getStringProperty()).isEqualTo("default"); // from SubConfig property default

        assertThat(config.getWithDefaultsAnnotation().getIntProperty()).isEqualTo(2); // from method default annotation
        assertThat(config.getWithDefaultsAnnotation().getStringProperty()).isEqualTo("withDefaults"); // from method default annotation

        assertThat(config.getListWithoutDefaultsAnnotation().size()).isEqualTo(5); // size from value source

        List<SubConfig> listWithDefaultsAnnotation = config.getListWithDefaultsAnnotation();
        assertThat(listWithDefaultsAnnotation.size()).isEqualTo(5); // size from value source
        assertThat(listWithDefaultsAnnotation.get(0).getIntProperty()).isEqualTo(11); // from value source
        assertThat(listWithDefaultsAnnotation.get(0).getStringProperty()).isEqualTo("fromSource-1"); // from value source
        assertThat(listWithDefaultsAnnotation.get(1).getIntProperty()).isEqualTo(22); // from value source
        assertThat(listWithDefaultsAnnotation.get(1).getStringProperty()).isEqualTo("fromSource-2"); // from value source
        assertThat(listWithDefaultsAnnotation.get(2).getIntProperty()).isEqualTo(3); // from method default annotation
        assertThat(listWithDefaultsAnnotation.get(2).getStringProperty()).isEqualTo("default"); // from SubConfig property default because of SKIP in method annotation
        assertThat(listWithDefaultsAnnotation.get(3).getIntProperty()).isEqualTo(4); // from method default annotation
        assertThat(listWithDefaultsAnnotation.get(3).getStringProperty()).isEqualTo("withDefaultsList-4"); // from method default annotation
        assertThat(listWithDefaultsAnnotation.get(4).getIntProperty()).isEqualTo(1); // from SubConfig property default
        assertThat(listWithDefaultsAnnotation.get(4).getStringProperty()).isEqualTo("default"); // from SubConfig property default
    }

    @Test
    public void shouldProvideValuesFromValueSourceAndResolvePrefixes() {
        // given
        when(source.getValue(anyString(), any())).thenReturn(absent());
        when(source.getValue("config.withoutDefaults.int", null)).thenReturn(present("10"));
        when(source.getValue("config.withoutDefaults.string", null)).thenReturn(present("withoutDefaults-fromSource"));
        when(source.getValue("config.withDefaults.int", null)).thenReturn(present("20"));
        when(source.getValue("config.withDefaults.string", null)).thenReturn(present("withDefaults-fromSource"));
        when(source.getValue("config.listWithoutDefaults.size", null)).thenReturn(present("2"));
        when(source.getValue("config.listWithDefaults.size", null)).thenReturn(present("1"));
        when(source.getValue("config.listWithDefaults.int", null)).thenReturn(present("30"));
        when(source.getValue("config.listWithDefaults.string", null)).thenReturn(present("listWithDefaults-fromSource"));

        // when
        ConfigWithPrefixes config = factory.createConfiguration(ConfigWithPrefixes.class, source);

        // then
        assertThat(config.getWithoutDefaultsAnnotation().getIntProperty()).isEqualTo(10);
        assertThat(config.getWithoutDefaultsAnnotation().getStringProperty()).isEqualTo("withoutDefaults-fromSource");

        assertThat(config.getWithDefaultsAnnotation().getIntProperty()).isEqualTo(20);
        assertThat(config.getWithDefaultsAnnotation().getStringProperty()).isEqualTo("withDefaults-fromSource");

        assertThat(config.getListWithoutDefaultsAnnotation().size()).isEqualTo(2);

        List<SubConfig> listWithDefaultsAnnotation = config.getListWithDefaultsAnnotation();
        assertThat(listWithDefaultsAnnotation.size()).isEqualTo(1);
        assertThat(listWithDefaultsAnnotation.get(0).getIntProperty()).isEqualTo(30);
        assertThat(listWithDefaultsAnnotation.get(0).getStringProperty()).isEqualTo("listWithDefaults-fromSource");
        assertThat(config.getListSubConfigWithoutDefaultsAnnotation()).isEmpty();
    }


    @Key("config")
    public interface ConfigWithoutPrefixes {
        SubConfig getWithoutDefaultsAnnotation();

        @SubConfigDefault(intProperty = "2", stringProperty = "withDefaults")
        SubConfig getWithDefaultsAnnotation();

        List<SubConfig> getListWithoutDefaultsAnnotation();

        @SubConfigDefaults({
                @SubConfigDefault(intProperty = "1", stringProperty = "withDefaultsList-1"),
                @SubConfigDefault(intProperty = "2", stringProperty = DefaultsAnnotation.NULL),
                @SubConfigDefault(intProperty = "3", stringProperty = DefaultsAnnotation.SKIP),
                @SubConfigDefault(intProperty = "4", stringProperty = "withDefaultsList-4")
        })
        @DefaultSize(3)
        List<SubConfig> getListWithDefaultsAnnotation();
    }

    @Key("config")
    public interface ConfigWithPrefixes {
        @Key("withoutDefaults")
        SubConfig getWithoutDefaultsAnnotation();

        @Key("withDefaults")
        @SubConfigDefault(intProperty = "2", stringProperty = "withDefaults")
        SubConfig getWithDefaultsAnnotation();

        @Key("listWithoutDefaults")
        List<SubConfig> getListWithoutDefaultsAnnotation();

        @SubConfigDefaults({
                @SubConfigDefault(intProperty = "1", stringProperty = "withDefaultsList-1"),
                @SubConfigDefault(intProperty = "2", stringProperty = DefaultsAnnotation.NULL),
                @SubConfigDefault(intProperty = "3", stringProperty = DefaultsAnnotation.SKIP)
        })
        @Key("listWithDefaults")
        List<SubConfig> getListWithDefaultsAnnotation();

        @Key
        List<SubConfigWithoutDefaultsAnnotation> getListSubConfigWithoutDefaultsAnnotation();
    }

    @DefaultsAnnotation(SubConfigDefault.class)
    public interface SubConfig {
        @Key("int")
        @Default("1")
        int getIntProperty();

        @Key("string")
        @Default("default")
        String getStringProperty();
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    @Repeatable(SubConfigDefaults.class)
    @Documented
    public @interface SubConfigDefault {
        String intProperty() default "1";

        String stringProperty() default "defaultFromAnnotation";
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    @Documented
    public @interface SubConfigDefaults {
        SubConfigDefault[] value();
    }

    // sub-configurations without default annotations can also be used in the lists
    public interface SubConfigWithoutDefaultsAnnotation {
        @Key
        @Default("value")
        String getValue();
    }
}



