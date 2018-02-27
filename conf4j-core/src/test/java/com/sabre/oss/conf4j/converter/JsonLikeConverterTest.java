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

package com.sabre.oss.conf4j.converter;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.converter.JsonLikeConverter.*;
import static com.sabre.oss.conf4j.internal.utils.MapUtils.of;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("ConstantConditions")
public class JsonLikeConverterTest {

    public static final List<String> LIST_SIGNATURE = null;
    public static final Map<String, String> MAP_SIGNATURE = null;
    public static final Map<List<String>, Map<String, List<String>>> COMPLEX_SIGNATURE = null;
    public static final Map<List<String>, Map<String, List<BigDecimal>>> NOT_SUPPORTED_COMPLEX_SIGNATURE = null;

    public static final Type SUPPORTED_LIST_TYPE;
    public static final Type SUPPORTED_MAP_TYPE;
    public static final Type SUPPORTED_COMPLEX_TYPE;
    public static final Type NOT_SUPPORTED_COMPLEX_TYPE;

    static {
        SUPPORTED_LIST_TYPE = getFieldGenericType("LIST_SIGNATURE");
        SUPPORTED_MAP_TYPE = getFieldGenericType("MAP_SIGNATURE");
        SUPPORTED_COMPLEX_TYPE = getFieldGenericType("COMPLEX_SIGNATURE");
        NOT_SUPPORTED_COMPLEX_TYPE = getFieldGenericType("NOT_SUPPORTED_COMPLEX_SIGNATURE");
    }

    public static final String EMPTY_STRING = "";
    public static final String TEXT1 = "/n/r/b/t\\/,:}]";
    public static final String TEXT2 = "\"[{";

    private JsonLikeConverter typeConverter;

    @BeforeEach
    public void setUp() {
        typeConverter = createConverter(true);
    }

    @Test
    public void shouldProperlyIndicateSupportedTypes() {
        // when / then
        assertThat(typeConverter.isApplicable(SUPPORTED_LIST_TYPE, null)).isTrue();
        assertThat(typeConverter.isApplicable(SUPPORTED_MAP_TYPE, null)).isTrue();
        assertThat(typeConverter.isApplicable(SUPPORTED_COMPLEX_TYPE, null)).isTrue();
        assertThat(typeConverter.isApplicable(NOT_SUPPORTED_COMPLEX_TYPE, null)).isFalse();
    }

    @Test
    public void shouldProperlyConvertToJsonStringFromListAndBack() {
        // given
        boolean compact = false;
        Type type = SUPPORTED_LIST_TYPE;
        //when/then
        assertSymmetricConversion(compact, type, singletonList(null), '[' + JSON_NULL + ']');
        assertSymmetricConversion(compact, type, singletonList(EMPTY_STRING), '[' + json(EMPTY_STRING) + ']');
        assertSymmetricConversion(compact, type, asList(EMPTY_STRING, EMPTY_STRING), '[' + json(EMPTY_STRING) + ',' + json(EMPTY_STRING) + ']');
        assertSymmetricConversion(compact, type, asList(COMPACT_JSON_NULL, JSON_NULL), '[' + json(COMPACT_JSON_NULL) + ',' + json(JSON_NULL) + ']');
        assertSymmetricConversion(compact, type, asList(JSON_NULL, COMPACT_JSON_NULL, TEXT1, TEXT2, EMPTY_STRING, null), '['
                + json(JSON_NULL) + ','
                + json(COMPACT_JSON_NULL) + ','
                + json(TEXT1) + ','
                + json(TEXT2) + ','
                + json(EMPTY_STRING) + ','
                + JSON_NULL + ']');
    }

    @Test
    public void shouldProperlyConvertToCompactJsonStringFromListAndBack() {
        // given
        boolean compact = true;
        Type type = SUPPORTED_LIST_TYPE;
        //when/then
        assertSymmetricConversion(compact, type, singletonList(null), '[' + COMPACT_JSON_NULL + ']');
        assertSymmetricConversion(compact, type, singletonList(EMPTY_STRING), '[' + COMPACT_JSON_EMPTY + ']');
        assertSymmetricConversion(compact, type, asList(EMPTY_STRING, EMPTY_STRING), '[' + cjson(EMPTY_STRING) + ',' + cjson(EMPTY_STRING) + ']');
        assertSymmetricConversion(compact, type, asList(COMPACT_JSON_NULL, JSON_NULL), '[' + cjson(COMPACT_JSON_NULL) + ',' + cjson(JSON_NULL) + ']');
        assertSymmetricConversion(compact, type, asList(EMPTY_STRING, JSON_NULL, COMPACT_JSON_NULL, TEXT1, TEXT2, null, EMPTY_STRING), '['
                + cjson(EMPTY_STRING) + ','
                + cjson(JSON_NULL) + ','
                + cjson(COMPACT_JSON_NULL) + ','
                + cjson(TEXT1) + ','
                + cjson(TEXT2) + ','
                + COMPACT_JSON_NULL + ','
                + cjson(EMPTY_STRING) + ']');
    }

    @Test
    public void shouldProperlyConvertToJsonStringFromMapAndBack() {
        // given
        boolean compact = false;
        Type type = SUPPORTED_MAP_TYPE;
        // when/then
        assertSymmetricConversion(compact, type, null, null);
        assertSymmetricConversion(compact, type,
                new LinkedHashMap<String, String>() {{
                    put(TEXT2, TEXT1);
                    put(TEXT1, TEXT2);
                    put(EMPTY_STRING, null);
                    put(null, EMPTY_STRING);
                }}, '{' +
                        json(TEXT2) + ':' + json(TEXT1) + ','
                        + json(TEXT1) + ':' + json(TEXT2) + ','
                        + json(EMPTY_STRING) + ':' + JSON_NULL + ','
                        + JSON_NULL + ':' + json(EMPTY_STRING) + '}');
    }

    @Test
    public void shouldProperlyConvertToCompactJsonStringFromMapAndBack() {
        // given
        boolean compact = true;
        Type type = SUPPORTED_MAP_TYPE;
        // when/then
        assertSymmetricConversion(compact, type, null, null);
        assertSymmetricConversion(compact, type,
                new LinkedHashMap<String, String>() {{
                    put(TEXT2, TEXT1);
                    put(TEXT1, TEXT2);
                    put(EMPTY_STRING, null);
                    put(JSON_NULL, COMPACT_JSON_NULL);
                    put(null, EMPTY_STRING);
                }}, '{'
                        + cjson(TEXT2) + ':' + cjson(TEXT1) + ','
                        + cjson(TEXT1) + ':' + cjson(TEXT2) + ','
                        + cjson(EMPTY_STRING) + ':' + COMPACT_JSON_NULL + ','
                        + cjson(JSON_NULL) + ':' + cjson(COMPACT_JSON_NULL) + ','
                        + COMPACT_JSON_NULL + ':' + cjson(EMPTY_STRING) +
                        '}');
    }

    @Test
    public void shouldProperlyConvertToJsonStringFromComplexStructureAndBack() throws NoSuchFieldException {
        convertComplexStructureToStringAndBack(false);
    }

    @Test
    public void shouldProperlyConvertToCompactJsonStringFromComplexStructureAndBack() throws NoSuchFieldException {
        convertComplexStructureToStringAndBack(true);
    }

    private void convertComplexStructureToStringAndBack(boolean compact) {
        // given
        typeConverter = createConverter(compact);
        Map<List<String>, Map<String, List<String>>> in = of(
                singletonList("key1"), of(
                        "sub11", asList(EMPTY_STRING, "one", "two"),
                        "sub12", asList("one", null, EMPTY_STRING),
                        "sub13", null),
                asList("key2", null), of(
                        null, singletonList(null),
                        "sub22", null),
                null, null);
        // when
        String out = typeConverter.toString(SUPPORTED_COMPLEX_TYPE, in, null);
        Object outObj = typeConverter.fromString(SUPPORTED_COMPLEX_TYPE, out, null);
        // then
        assertThat(outObj).isEqualTo(in);
    }

    @Test
    public void shouldProperlyConsumeJsonNullsAndAddNullValuesToBuilder() {
        // given
        boolean compact = false;
        ObjectBuilder builder = Mockito.mock(ObjectBuilder.class);
        // when / then
        assertThat(typeConverter.consumeNull(JSON_NULL, 0, builder, compact)).isEqualTo(JSON_NULL.length());
        assertThat(typeConverter.consumeNull(JSON_NULL + ',', 0, builder, compact)).isEqualTo(JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL, 3, builder, compact)).isEqualTo(JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL + ',', 3, builder, compact)).isEqualTo(JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL + ':', 3, builder, compact)).isEqualTo(JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL + '}', 3, builder, compact)).isEqualTo(JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL + ']', 3, builder, compact)).isEqualTo(JSON_NULL.length());

        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL + ',', 2, builder, compact)).isEqualTo(0);
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL + '{', 3, builder, compact)).isEqualTo(0);
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL + '[', 3, builder, compact)).isEqualTo(0);
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL + '[', 3, builder, compact)).isEqualTo(0);

        verify(builder, times(7)).addValue(eq(null));
    }

    @Test
    public void shouldProperlyConsumeCompactJsonNullsAndAddNullValuesToBuilder() {
        // given
        boolean compact = true;
        ObjectBuilder builder = Mockito.mock(ObjectBuilder.class);
        // when / then
        assertThat(typeConverter.consumeNull(COMPACT_JSON_NULL, 0, builder, compact)).isEqualTo(COMPACT_JSON_NULL.length());
        assertThat(typeConverter.consumeNull(COMPACT_JSON_NULL + ',', 0, builder, compact)).isEqualTo(COMPACT_JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL, 3, builder, compact)).isEqualTo(COMPACT_JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL + ',', 3, builder, compact)).isEqualTo(COMPACT_JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL + ':', 3, builder, compact)).isEqualTo(COMPACT_JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL + '}', 3, builder, compact)).isEqualTo(COMPACT_JSON_NULL.length());
        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL + ']', 3, builder, compact)).isEqualTo(COMPACT_JSON_NULL.length());

        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL + ',', 2, builder, compact)).isEqualTo(0);
        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL + '{', 3, builder, compact)).isEqualTo(0);
        assertThat(typeConverter.consumeNull("ABC" + COMPACT_JSON_NULL + '{', 3, builder, compact)).isEqualTo(0);
        assertThat(typeConverter.consumeNull("ABC" + JSON_NULL + '[', 3, builder, compact)).isEqualTo(0);

        verify(builder, times(7)).addValue(eq(null));
    }

    protected void assertSymmetricConversion(boolean compactMode, Type type, Object object, String string) {
        assertProperConversionToString(compactMode, type, object, string);
        assertProperConversionFromString(compactMode, type, string, object);
    }

    protected void assertProperConversionToString(boolean compactMode, Type type, Object in, String expectedOut) {
        // given
        typeConverter = createConverter(compactMode);
        // when
        String out = typeConverter.toString(type, in, null);
        Object outObj = typeConverter.fromString(type, out, null);
        // then
        assertThat(out).isEqualTo(expectedOut);
        assertThat(outObj).isEqualTo(in);
    }

    protected void assertProperConversionFromString(boolean compactMode, Type type, String in, Object expectedOut) {
        // given
        typeConverter = createConverter(compactMode);
        // when
        Object out = typeConverter.fromString(type, in, null);
        String outStr = typeConverter.toString(type, out, null);
        // then
        assertThat(out).isEqualTo(expectedOut);
        assertThat(outStr).isEqualTo(in);
    }

    private static Type getFieldGenericType(String fieldName) {
        try {
            return JsonLikeConverterTest.class.getDeclaredField(fieldName).getGenericType();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static String json(String text) {
        return escapeJson(text, true);
    }

    private static String cjson(String text) {
        return escapeCompactJson(text);
    }

    private static String escapeJson(String text) {
        return escapeJson(text, false);
    }

    private static String escapeJson(String text, boolean appendDoubleQuotes) {
        return appendDoubleQuotes
                ? '"' + StringEscapeUtils.escapeJson(text) + '"'
                : StringEscapeUtils.escapeJson(text);
    }

    private static String escapeCompactJson(String text) {
        return JsonLikeEscapeUtils.ESCAPE_COMPACT_JSON.translate(text);
    }

    private JsonLikeConverter createConverter(boolean compactMode) {
        return new JsonLikeConverter(new StringConverter(false), compactMode);
    }
}
