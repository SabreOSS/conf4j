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

package com.sabre.oss.conf4j.spring;

import com.sabre.oss.conf4j.internal.config.DynamicConfiguration;
import com.sabre.oss.conf4j.source.MapConfigurationSource;
import com.sabre.oss.conf4j.spring.model.SpringBean;
import com.sabre.oss.conf4j.spring.model.SpringBeanConfiguration;
import com.sabre.oss.conf4j.spring.model.inheritance.CommonConfiguration;
import com.sabre.oss.conf4j.spring.model.keyprefix.SpringBeanConfigurationWithoutKeyPrefix;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@ContextConfiguration(locations = "classpath:SpringConfigurationFactoryTest/conf4j.spring.test.xml")
public class SpringConfigurationFactoryTest extends AbstractContextTest {

    @Test
    public void shouldHandleStandardPropertiesForSpringBeanGeneration() {
        SpringBeanConfiguration configuration = applicationContext.getBean(SpringBeanConfiguration.class);

        assertThat(configuration).isNotNull().isInstanceOf(DynamicConfiguration.class);
        // it has taken the value from property "bean.level.prefix.string01" in file "conf4j.test.properties"
        assertThat(configuration.getString01()).isEqualTo("customValue01");
        // there is no override of property "bean.level.prefix.string02" so that default value is returned
        assertThat(configuration.getString02()).isEqualTo("defaultValue02");
        // Handle primitive types
        assertThat(configuration.getBooleanPrimitive()).isTrue();
        assertThat(configuration.getDefaultBooleanPrimitive()).isFalse();
        // Handle wrapped types
        assertThat(configuration.getBooleanObject()).isTrue();
        assertThat(configuration.getDefaultBooleanObject()).isFalse();
        // Handle parameterized collections
        assertThat(configuration.getListOfStrings()).hasSize(2).containsOnly("A", "B");
        assertThat(configuration.getMapOfStringToString()).hasSize(2).containsOnly(entry("A", "1"), entry("B", "2"));
        // Handle non abstract methods
        assertThat(configuration.getComplexProperty()).isEqualTo("hiddenValue");
        // Handle raw strings
        assertThat(configuration.getURL()).isEqualTo("http://127.0.0.1");
        // Handle sorted map
        assertThat(configuration.getSortedMap()).containsExactly(
                entry("A", "B"),
                entry("C", "D"),
                entry("E", "F")
        );
        // Handle sub configurations that are spring beans
        assertThat(configuration.getSpringItemsConfiguration()).hasSize(2);
        assertThat(configuration.getSpringItemsConfiguration()).extracting("property1").isEqualTo(asList("AA", "BB"));
        assertThat(configuration.getSpringItemsConfiguration()).extracting("property2").isEqualTo(asList("property2DefaultValue", "property2DefaultValue"));
    }

    @Test
    public void shouldHandleAutowiredConfiguration() {
        SpringBean bean = applicationContext.getBean(SpringBean.class);
        SpringBeanConfiguration configuration = applicationContext.getBean(SpringBeanConfiguration.class);

        assertThat(configuration).isEqualTo(bean.getConfig());
    }

    @Test
    public void shouldCreateSpringBeanConfigurationWithoutKeyPrefixAnnotation() {
        SpringBeanConfigurationWithoutKeyPrefix configuration = applicationContext.getBean(SpringBeanConfigurationWithoutKeyPrefix.class);
        // Get value from values source
        assertThat(configuration.getPropertyName()).isEqualTo("CUSTOM_VALUE");
    }

    @Test
    public void shouldCreateNestedSpringBeanConfigurations() {
        CommonConfiguration configuration = applicationContext.getBean("commonConfiguration", CommonConfiguration.class);
        // Get nested configuration default property
        assertThat(configuration.getNested().getCommonNestedProperty()).isEqualTo("common");
    }

    @Test
    @DirtiesContext
    public void shouldHandleDefaultValuesForComplexMaps() {
        SpringBeanConfiguration configuration = applicationContext.getBean(SpringBeanConfiguration.class);
        ((DynamicConfiguration) configuration).setConfigurationSource(new MapConfigurationSource(of(
                "springBeanConfigurationPrefix.mapWithListAsValue", "{1:[a,b],2:[a,b,c]}",
                "springBeanConfigurationPrefix.mapWithMapAsValue", "{1:{a:b},2:{c:d,e:f}}"
        )));

        assertThat(configuration.getComplexMap()).containsExactly(
                entry("1", of("a", "b")),
                entry("2", of("c", "d", "e", "f")));

        assertThat(configuration.getComplexListMap()).containsExactly(
                entry("1", asList("a", "b")),
                entry("2", asList("a", "b", "c")));
    }

    private static <K, V> Map<K, V> of(K k, V v) {
        Map<K, V> map = new HashMap<>();
        map.put(k, v);
        return map;
    }

    private static <K, V> Map<K, V> of(K k, V v, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k, v);
        map.put(k2, v2);
        return map;
    }
}
