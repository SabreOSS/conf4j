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

package com.sabre.oss.conf4j.converter;

import com.sabre.oss.conf4j.converter.JaxbConverter.JaxbPool;
import com.sabre.oss.conf4j.converter.xml.XmlRootConfiguration01;
import org.junit.Test;

import javax.xml.validation.Schema;

import static org.assertj.core.api.Assertions.assertThat;

public class JaxbPoolTest {

    @Test
    public void shouldReturnSchemaFromValidPackageInfo() throws Exception {
        // given
        JaxbPool jaxbPool = new JaxbPool(XmlRootConfiguration01.class);
        // when
        Schema schema = jaxbPool.readXsdSchema(JaxbPoolTest.class);
        // then
        assertThat(schema).isNull();
    }

    @Test
    public void shouldReturnNullSchemaWhenPackageInfoIsNotAvailable() throws Exception {
        // given
        JaxbPool jaxbPool = new JaxbPool(XmlRootConfiguration01.class);
        // when
        Schema schema = jaxbPool.readXsdSchema(XmlRootConfiguration01.class);
        // then
        assertThat(schema).isNotNull();
    }
}
