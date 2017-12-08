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

package com.sabre.oss.conf4j.spring.source;

import com.sabre.oss.conf4j.source.ConfigurationSource;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(locations = "classpath*:META-INF/com/sabre/oss/conf4j/conf4j.xml")
@TestPropertySource({
        "classpath:PropertySourceConfigurationSourceTest/a.properties",
        "classpath:PropertySourceConfigurationSourceTest/b.properties"})
public class PropertySourceConfigurationSourceTest extends AbstractJUnit4SpringContextTests {
    @Autowired
    private ConfigurationSource source;

    @Test
    public void shouldResolvePropertyPlaceholdersSetWithCorrectOrder() {
        // then
        assertThat(source.getValue("property.only.in.A", null)).isEqualTo(present("A"));
        assertThat(source.getValue("property.only.in.B", null)).isEqualTo(present("B"));
        assertThat(source.getValue("property.in.A.and.B", null)).isEqualTo(present("B"));
    }
}
