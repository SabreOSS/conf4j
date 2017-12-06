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

package com.sabre.oss.conf4j.converter.xml;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.converter.xml.model.NonXmlConfiguration;
import com.sabre.oss.conf4j.converter.xml.model.XmlRootConfiguration01;
import com.sabre.oss.conf4j.converter.xml.model.XmlRootConfiguration02;
import com.sabre.oss.conf4j.converter.xml.model.XmlSubConfiguration;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

public class JaxbConverterTest {

    TypeConverter<Object> jaxbTypeConverter = new JaxbConverter<>();

    @Test
    public void shouldBeApplicableForMultipleXmlTypes() {
        assertThat(jaxbTypeConverter.isApplicable(XmlRootConfiguration01.class, null)).isTrue();
        assertThat(jaxbTypeConverter.isApplicable(XmlRootConfiguration02.class, null)).isTrue();
    }

    @Test
    public void shouldNotBeApplicableForNonXmlTypes() {
        assertThat(jaxbTypeConverter.isApplicable(NonXmlConfiguration.class, null)).isFalse();
    }

    @Test
    public void shouldReturnNullObjectForBlankXML() {
        // given
        String resource = " ";
        // when
        XmlRootConfiguration01 xmlRootConfiguration01 = (XmlRootConfiguration01) jaxbTypeConverter.fromString(XmlRootConfiguration01.class, resource, null);
        // then
        assertNull(xmlRootConfiguration01);
    }

    @Test
    public void shouldReturnNullObjectForNullXML() {
        // given
        String resource = null;
        // when
        XmlRootConfiguration01 xmlRootConfiguration01 = (XmlRootConfiguration01) jaxbTypeConverter.fromString(XmlRootConfiguration01.class, resource, null);
        // then
        assertNull(xmlRootConfiguration01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailValidationWhenReadingXMLNotCompliantWithTheSchema() {
        // given - "ConfigurationVer" element is inconsistent with the schema
        String resource = lodXml("/JaxbTypeAdapterTest/XmlRootConfiguration01.invalid.xml");
        // when
        jaxbTypeConverter.fromString(XmlRootConfiguration01.class, resource, null);
        // then expect exception
    }

    @Test
    public void shouldReturnProperPojoForValidXML() {
        // given
        String resource = lodXml("/JaxbTypeAdapterTest/XmlRootConfiguration01.xml");
        // when
        XmlRootConfiguration01 config01 = (XmlRootConfiguration01) jaxbTypeConverter.fromString(XmlRootConfiguration01.class, resource, null);
        // then
        assertThat(config01.getConfigurationName()).isEqualTo("ROOT01");
        assertThat(config01.getXmlSubConfigurations()).hasSize(2);
        assertThat(config01.getXmlSubConfigurations().get(0).getVersion()).isEqualTo(1);
        assertThat(config01.getXmlSubConfigurations().get(1).getVersion()).isEqualTo(2);
    }

    @Test
    public void shouldReturnProperPojoForSecondValidXML() {
        // given
        String resource = lodXml("/JaxbTypeAdapterTest/XmlRootConfiguration02.xml");
        // when
        XmlRootConfiguration02 config01 = (XmlRootConfiguration02) jaxbTypeConverter.fromString(XmlRootConfiguration02.class, resource, null);
        // then
        assertThat(config01.getConfigurationName()).isEqualTo("ROOT02");
        assertThat(config01.getXmlSubConfigurations()).hasSize(2);
        assertThat(config01.getXmlSubConfigurations().get(0).getVersion()).isEqualTo(3);
        assertThat(config01.getXmlSubConfigurations().get(1).getVersion()).isEqualTo(4);
    }

    @Test
    public void shouldMarshalAndDemarshalXmlObject() {
        // given
        XmlRootConfiguration01 before = new XmlRootConfiguration01()
                .withConfigurationName("DEV")
                .withConfigurationVersion(new XmlSubConfiguration().withVersion(1))
                .withConfigurationVersion(new XmlSubConfiguration().withVersion(2));
        // when
        String configurationString = remove(jaxbTypeConverter.toString(XmlRootConfiguration01.class, before, null), System.lineSeparator());
        XmlRootConfiguration01 after = (XmlRootConfiguration01) jaxbTypeConverter.fromString(XmlRootConfiguration01.class, configurationString, null);
        // then
        assertThat(before).isEqualTo(after);
    }

    private String lodXml(String resource) {
        URL url = getClass().getResource(resource);
        if (url == null) {
            throw new IllegalArgumentException("Resource " + resource + " not found");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
