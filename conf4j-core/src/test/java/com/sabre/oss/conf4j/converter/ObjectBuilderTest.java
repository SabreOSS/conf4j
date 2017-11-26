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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class ObjectBuilderTest {

    @Test
    public void shouldProperlyBuildMap() {
        // given / when
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) new ObjectBuilder()
                .beginMap()
                .addKey("string").addValue("string value")
                .addKey("boolean").addValue(true)
                .addKey("nullable").addValue(null)
                .addKey(null).addValue(null)
                .endMap()
                .build();

        // then
        assertThat(map).containsOnly(
                entry("string", "string value"),
                entry("boolean", true),
                entry("nullable", null),
                entry(null, null));
    }

    @Test
    public void shouldProperlyBuildList() {
        // given / when
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) new ObjectBuilder()
                .beginList()
                .addValue("string value")
                .addValue(true)
                .addValue(null)
                .endList()
                .build();

        // then
        assertThat(list).isEqualTo(asList("string value", true, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProperlyCreateMapOfListsWithOverwrittenKey() {
        // given / when
        Map<String, List<String>> map = (Map<String, List<String>>) new ObjectBuilder()
                .beginMap()
                .addKey("values").beginList().addValue("value1").addValue("value2").endList()
                .addKey("values").beginList().addValue("value3").addValue("value4").endList()
                .endMap()
                .build();

        // then
        assertThat(map).containsOnly(entry("values", asList("value3", "value4")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProperlyCreateNestedMap() {
        // given / when
        Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) new ObjectBuilder()
                .beginMap()
                .addKey("object").beginMap().addKey("property").addValue("property value").endMap()
                .endMap()
                .build();

        // then
        assertThat(map).containsOnlyKeys("object");
        assertThat(map.get("object")).containsOnly(entry("property", "property value"));
    }

    @Test
    public void shouldFailInCaseClosingMapOnEmptyBuilder() {
        assertThatThrowExceptionOfType(
                // when
                new ObjectBuilder()::endMap,
                // then
                IllegalStateException.class);
    }

    @Test
    public void shouldFailInCaseClosingListOnEmptyBuilder() {
        assertThatThrowExceptionOfType(
                // when
                new ObjectBuilder()::endList,
                // then
                IllegalStateException.class);
    }

    @Test
    public void shouldFailInCaseClosingTopLevelMap() {
        assertThatThrowExceptionOfType(
                // when
                new ObjectBuilder().beginMap().endMap()::endMap,
                // then
                IllegalStateException.class);
    }

    @Test
    public void shouldFailInCaseClosingTopLevelList() {
        assertThatThrowExceptionOfType(
                // when
                new ObjectBuilder().beginList().endList()::endList,
                // then
                IllegalStateException.class);
    }

    @Test
    public void shouldFailInCaseEndingObjectWhenArrayWasBegun() {
        assertThatThrowExceptionOfType(
                // when
                new ObjectBuilder().beginList()::endMap,
                // then
                IllegalStateException.class);
    }

    @Test
    public void shouldFailInCaseEndingArrayWhenObjectWasBegun() {
        assertThatThrowExceptionOfType(
                // when
                new ObjectBuilder().beginMap()::endList,
                // then
                IllegalStateException.class);
    }

    private void assertThatThrowExceptionOfType(Runnable runnable, Class<? extends Exception> expectedException) {
        try {
            runnable.run();
        } catch (Exception e) {
            assertThat(expectedException).isEqualTo(expectedException);
        }
    }
}
