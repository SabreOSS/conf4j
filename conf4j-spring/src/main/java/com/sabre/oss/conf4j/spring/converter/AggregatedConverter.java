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

package com.sabre.oss.conf4j.spring.converter;

import com.sabre.oss.conf4j.converter.DefaultTypeConverters;
import com.sabre.oss.conf4j.converter.DelegatingConverterFactory;
import com.sabre.oss.conf4j.converter.TypeConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.converter.DefaultTypeConverters.createCompositeConverter;
import static com.sabre.oss.conf4j.converter.DefaultTypeConverters.getDefaultBaseConverters;
import static com.sabre.oss.conf4j.converter.DefaultTypeConverters.getDefaultDelegatingConverterFactories;

/**
 * Aggregates all {@link TypeConverter}s available in the context
 * and converters provided by {@link DefaultTypeConverters#getDefaultBaseConverters()}.
 * They are composed using {@link DefaultTypeConverters#createCompositeConverter(List, List)}
 * and {@code AggregatedConverter} delegates to it.
 */
public class AggregatedConverter implements TypeConverter<Object> {
    private TypeConverter<Object> converter;

    private List<TypeConverter<?>> autowired;
    private List<DelegatingConverterFactory> autowiredFactories;

    public void setAutowired(List<TypeConverter<?>> autowired) {
        this.autowired = autowired;
    }

    public void setAutowiredFactories(List<DelegatingConverterFactory> autowiredFactories) {
        this.autowiredFactories = autowiredFactories;
    }

    public void initialize() {
        List<TypeConverter<?>> allBaseConverters = prepareBaseConverters();
        List<DelegatingConverterFactory> allDelegatingConverterFactories = prepareDelegatingConverterFactories();
        converter = createCompositeConverter(allBaseConverters, allDelegatingConverterFactories);
    }

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        return converter.isApplicable(type, attributes);
    }

    @Override
    public Object fromString(Type type, String value, Map<String, String> attributes) {
        return converter.fromString(type, value, attributes);
    }

    @Override
    public String toString(Type type, Object value, Map<String, String> attributes) {
        return converter.toString(type, value, attributes);
    }

    private List<TypeConverter<?>> prepareBaseConverters() {
        List<TypeConverter<?>> allConverters = new ArrayList<>();
        if (autowired != null) {
            allConverters.addAll(autowired);
        }
        allConverters.addAll(getDefaultBaseConverters());
        return allConverters;
    }

    private List<DelegatingConverterFactory> prepareDelegatingConverterFactories() {
        List<DelegatingConverterFactory> allConverters = new ArrayList<>();
        if (autowiredFactories != null) {
            allConverters.addAll(autowiredFactories);
        }
        allConverters.addAll(getDefaultDelegatingConverterFactories());
        return allConverters;
    }
}
