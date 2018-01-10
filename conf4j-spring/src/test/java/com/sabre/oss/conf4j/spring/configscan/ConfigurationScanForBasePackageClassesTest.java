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

package com.sabre.oss.conf4j.spring.configscan;

import com.sabre.oss.conf4j.spring.annotation.ConfigurationScan;
import com.sabre.oss.conf4j.spring.configscan.model.Bean;
import com.sabre.oss.conf4j.spring.configscan.model.ConfigurationWithName;
import com.sabre.oss.conf4j.spring.configscan.model.RootConfiguration;
import com.sabre.oss.conf4j.spring.configscan.model.SubConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ConfigurationScan(basePackageClasses = RootConfiguration.class)
@ContextConfiguration(classes = ConfigurationScanForBasePackageClassesTest.class)
public class ConfigurationScanForBasePackageClassesTest extends AbstractConfigurationScanTest {
    @Autowired
    private RootConfiguration rootConfiguration;

    @Test
    public void shouldRegisterOnlyRootConfigurationClasses() {
        isRegistered(RootConfiguration.class, "rootConfiguration");
        isRegistered(ConfigurationWithName.class, "customPrefix.configurationWithName");
        isNotRegistered(SubConfiguration.class);
        isNotRegistered(Bean.class);
    }
}
