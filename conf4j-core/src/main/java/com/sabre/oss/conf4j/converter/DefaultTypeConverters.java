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

import com.sabre.oss.conf4j.converter.composite.JsonLikeTypeConverter;
import com.sabre.oss.conf4j.converter.standard.BigDecimalTypeConverter;
import com.sabre.oss.conf4j.converter.standard.BooleanTypeConverter;
import com.sabre.oss.conf4j.converter.standard.ByteTypeConverter;
import com.sabre.oss.conf4j.converter.standard.CharacterTypeConverter;
import com.sabre.oss.conf4j.converter.standard.DoubleTypeConverter;
import com.sabre.oss.conf4j.converter.standard.DurationTypeConverter;
import com.sabre.oss.conf4j.converter.standard.EnumTypeConverter;
import com.sabre.oss.conf4j.converter.standard.EscapingStringTypeConverter;
import com.sabre.oss.conf4j.converter.standard.FloatTypeConverter;
import com.sabre.oss.conf4j.converter.standard.IntegerTypeConverter;
import com.sabre.oss.conf4j.converter.standard.LocalDateTimeTypeConverter;
import com.sabre.oss.conf4j.converter.standard.LongTypeConverter;
import com.sabre.oss.conf4j.converter.standard.PatternTypeConverter;
import com.sabre.oss.conf4j.converter.standard.PeriodTypeConverter;
import com.sabre.oss.conf4j.converter.standard.ShortTypeConverter;
import com.sabre.oss.conf4j.converter.xml.JaxbTypeConverter;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * A utility class which configures {@link TypeConverter} with the default set of type converters.
 */
public final class DefaultTypeConverters {

    private static final TypeConverter<Object> DEFAULT_AGGREGATE_TYPE_CONVERTER = prepareDefaultTypeConverter();

    private DefaultTypeConverters() {
    }

    /**
     * Returns pre-configured aggregate {@link TypeConverter}.
     *
     * @return {@link TypeConverter}
     */
    public static TypeConverter<Object> getDefaultTypeConverter() {
        return DEFAULT_AGGREGATE_TYPE_CONVERTER;
    }

    /**
     * Prepares default aggregate type converter that is able to handle all simple types and
     * any compositions of {@link List}s, {@code Map}s and supported simple types.
     *
     * @return aggregated {@link TypeConverter}<{@link Object}>
     */
    private static TypeConverter<Object> prepareDefaultTypeConverter() {
        TypeConverter<Object> simpleTypeConverters = new ChainedTypeConverter(asList(
                new LocalDateTimeTypeConverter(),
                new EscapingStringTypeConverter(),
                new BooleanTypeConverter(),
                new CharacterTypeConverter(),
                new IntegerTypeConverter(),
                new DoubleTypeConverter(),
                new FloatTypeConverter(),
                new ByteTypeConverter(),
                new ShortTypeConverter(),
                new LongTypeConverter(),
                new EnumTypeConverter(),
                new DurationTypeConverter(),
                new PeriodTypeConverter(),
                new BigDecimalTypeConverter(),
                new PatternTypeConverter(),
                new JaxbTypeConverter<>()
        ));

        JsonLikeTypeConverter jsonLikeTypeConverter = new JsonLikeTypeConverter(simpleTypeConverters);
        return new ChainedTypeConverter(asList(
                simpleTypeConverters,
                jsonLikeTypeConverter));
    }
}
