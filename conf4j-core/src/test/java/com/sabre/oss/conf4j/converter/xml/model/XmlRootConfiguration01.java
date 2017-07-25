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

package com.sabre.oss.conf4j.converter.xml.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "XmlRootConfiguration01")
@XmlType(propOrder = {"configurationName", "xmlSubConfigurations"})
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRootConfiguration01 {
    @XmlElement(name = "ConfigurationName")
    private String configurationName;
    @XmlElement(name = "XmlSubConfiguration")
    private List<XmlSubConfiguration> xmlSubConfigurations = new ArrayList<>();

    public XmlRootConfiguration01 withConfigurationName(final String configurationName) {
        this.configurationName = configurationName;
        return this;
    }

    public XmlRootConfiguration01 withConfigurationVersion(XmlSubConfiguration xmlSubConfiguration) {
        this.xmlSubConfigurations.add(xmlSubConfiguration);
        return this;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public List<XmlSubConfiguration> getXmlSubConfigurations() {
        return xmlSubConfigurations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        XmlRootConfiguration01 that = (XmlRootConfiguration01) o;
        return Objects.equals(configurationName, that.configurationName) &&
                Objects.equals(xmlSubConfigurations, that.xmlSubConfigurations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configurationName, xmlSubConfigurations);
    }
}
