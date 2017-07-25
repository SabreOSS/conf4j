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

package com.sabre.oss.conf4j.source;

import org.junit.Before;
import org.junit.Test;

import static com.sabre.oss.conf4j.internal.utils.MapUtils.of;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MultiConfigurationValuesSourceTest {
    static final String A_KEY = "A";
    static final String B_KEY = "B";

    private MultiConfigurationValuesSource source;

    @Before
    public void before() {
        source = new MultiConfigurationValuesSource(asList(
                new MapConfigurationValuesSource(of(A_KEY, A_KEY)),
                new MapConfigurationValuesSource(of(A_KEY, A_KEY + A_KEY, B_KEY, B_KEY))
        ));
    }

    @Test
    public void shouldReturnValuesFromProperSource() {
        assertThat(source.getValue(A_KEY).get()).isEqualTo(A_KEY);
        assertThat(source.getValue(B_KEY).get()).isEqualTo(B_KEY);
    }

    @Test
    public void shouldFindValuesInProperSource() {
        assertThat(source.findEntry(asList("NotExistingKey", B_KEY))).isEqualTo(new ConfigurationEntry(B_KEY, B_KEY));
    }
}
