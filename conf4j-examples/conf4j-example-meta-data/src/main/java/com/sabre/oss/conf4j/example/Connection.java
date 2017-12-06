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

package com.sabre.oss.conf4j.example;

import com.sabre.oss.conf4j.annotation.Configuration;
import com.sabre.oss.conf4j.annotation.Description;
import com.sabre.oss.conf4j.annotation.Key;

@Configuration
@Key
@Description("Connection configuration")
// Configuration values for all properties defined in this configuration are retrieved from 'application.properties'.
// SourceFileAwareConfigurationSource understands 'file' meta-data attribute and uses the file specified by
// 'file' attribute's value as a source of configuration values.
// @SourceFile("application.properties") is more convenient and expressive way of defining meta-data attributes than @Meta.
// This declaration is equivalent to @Meta(name="file", value="application.properties")
@SourceFile("application.properties")
public interface Connection {
    String getUrl();

    String getLogin();

    // It is possible to override meta-data attribute for on the configuration property.
    // In this case 'file' meta-data attribute's value is changed from 'application.properties' to 'secrets.properties'.
    @SourceFile("secrets.properties")
    String getPassword();
}
