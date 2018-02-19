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

package com.sabre.oss.conf4j.source;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


public class PropertiesConfigurationSourceTest {
    private File file;
    private Path filePath;

    @BeforeEach
    public void createTempDirectoryWithFile() throws IOException {
        filePath = createTempDirectory("tmp");
        file = new File(filePath.toString() + "\\sample.properties");
        file.createNewFile();
    }

    @AfterEach
    public void deleteTempDirectoryWithFile() {
        file.deleteOnExit();
        filePath.toFile().deleteOnExit();
    }

    @Test
    public void shouldAdaptPropertiesToConfigurationSource() {
        // given
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        properties.setProperty("key2", "value2");
        // when
        PropertiesConfigurationSource source = new PropertiesConfigurationSource(properties);

        // then
        assertThat(source.getAllConfigurationEntries().iterator()).hasSize(properties.size());
        assertThat(source.getValue("key1", null).get()).isEqualTo(properties.getProperty("key1"));
        assertThat(source.getValue("key2", null).get()).isEqualTo(properties.getProperty("key2"));
        assertThat(source.getValue("non-existing-key", null).isAbsent()).isTrue();
    }

    @Test
    public void shouldLoadPropertiesFromFile() throws IOException {
        // when
        PropertiesConfigurationSource source = new PropertiesConfigurationSource(file.getAbsolutePath());

        // then
        assertThat(source.getAllConfigurationEntries().iterator()).isEmpty();
    }
}
