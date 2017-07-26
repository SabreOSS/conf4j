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

package com.sabre.oss.conf4j.source;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PropertiesConfigurationValuesSourceTest {
    private TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldAdaptPropertiesToValuesSource() {
        // given
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        properties.setProperty("key2", "value2");

        // when
        PropertiesConfigurationValuesSource source = new PropertiesConfigurationValuesSource(properties);

        // then
        assertThat(source.getAllConfigurationEntries().iterator()).hasSize(properties.size());
        assertThat(source.getValue("key1").get()).isEqualTo(properties.getProperty("key1"));
        assertThat(source.getValue("key2").get()).isEqualTo(properties.getProperty("key2"));
        assertThat(source.getValue("non-existing-key").isAbsent()).isTrue();
    }

    @Test
    public void shouldLoadPropertiesFromFile() throws IOException {
        // given
        folder.create();
        File file = folder.newFile("sample.properties");
        // when
        PropertiesConfigurationValuesSource source = new PropertiesConfigurationValuesSource(file.getAbsolutePath());

        // then
        assertThat(source.getAllConfigurationEntries().iterator()).isEmpty();
    }
}
