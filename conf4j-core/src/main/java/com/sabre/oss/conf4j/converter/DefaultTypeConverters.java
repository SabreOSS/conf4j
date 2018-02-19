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

import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * A utility class which configures {@link TypeConverter} with the default set of type converters.
 */
public final class DefaultTypeConverters {
    private static final List<TypeConverter<?>> DEFAULT_BASE_CONVERTERS = unmodifiableList(asList(
            new StringConverter(),
            new BooleanConverter(),
            new CharacterConverter(),
            new IntegerConverter(),
            new DoubleConverter(),
            new FloatConverter(),
            new ByteConverter(),
            new ShortConverter(),
            new LongConverter(),
            new EnumConverter(),
            new DurationConverter(),
            new LocalDateTimeConverter(),
            new InstantConverter(),
            new OffsetDateTimeConverter(),
            new PeriodConverter(),
            new BigDecimalConverter(),
            new PatternConverter(),
            new UrlConverter(),
            new CurrencyConverter()
    ));

    private static final List<DecoratingConverterFactory> DEFAULT_DELEGATING_CONVERTER_FACTORIES = unmodifiableList(singletonList(
            createDelegatingFactory(JsonLikeConverter::new)
    ));

    private static final TypeConverter<Object> DEFAULT_AGGREGATE_CONVERTER = prepareDefaultTypeConverter();

    private DefaultTypeConverters() {
    }

    /**
     * Returns pre-configured aggregate {@link TypeConverter}.
     *
     * @return {@link TypeConverter}
     */
    public static TypeConverter<Object> getDefaultTypeConverter() {
        return DEFAULT_AGGREGATE_CONVERTER;
    }

    /**
     * Return list of standard, base {@link TypeConverter}.
     *
     * @return list of base converters.
     */
    public static List<TypeConverter<?>> getDefaultBaseConverters() {
        return DEFAULT_BASE_CONVERTERS;
    }

    /**
     * Return list of standard {@link DecoratingConverterFactory}.
     *
     * @return list of decorating converter factories.
     */
    public static List<DecoratingConverterFactory> getDefaultDelegatingConverterFactories() {
        return DEFAULT_DELEGATING_CONVERTER_FACTORIES;
    }

    /**
     * Create composite converter.
     * Each converter in {@code decoratingConverterFactories} decorates its successor in the list.
     * The last converter decorates {@link ChainedTypeConverter} composed of {@code baseConverters}.
     *
     * @param baseConverters               base type converters.
     * @param decoratingConverterFactories decorating converter factories.
     * @return composite converter.
     */
    @SuppressWarnings("unchecked")
    public static TypeConverter<Object> createCompositeConverter(
            List<TypeConverter<?>> baseConverters,
            List<DecoratingConverterFactory> decoratingConverterFactories) {
        requireNonNull(baseConverters, "baseConverters cannot be null");
        requireNonNull(decoratingConverterFactories, "decoratingConverterFactories cannot be null");

        TypeConverter<?> typeConverter = new ChainedTypeConverter(baseConverters);

        for (DecoratingConverterFactory factory : decoratingConverterFactories) {
            typeConverter = factory.create(typeConverter);
        }

        return (TypeConverter<Object>) typeConverter;
    }

    /**
     * Creates decorating converter factory as {@link ChainedTypeConverter} composed of parameter
     * and {@code delegate} applied to this parameter.
     *
     * @param delegate function creating converter decorator.
     * @return decorating converter factory.
     */
    public static DecoratingConverterFactory createDelegatingFactory(Function<TypeConverter<?>, TypeConverter<?>> delegate) {
        return (f) -> new ChainedTypeConverter(asList(f, delegate.apply(f)));
    }

    /**
     * Prepares default aggregate type converter that is able to handle all simple types and
     * any compositions of {@link List}s, {@code Map}s and supported simple types.
     *
     * @return aggregated {@link TypeConverter}<{@link Object}>
     */
    private static TypeConverter<Object> prepareDefaultTypeConverter() {
        List<TypeConverter<?>> defaultBaseTypeConverters = getDefaultBaseConverters();
        List<DecoratingConverterFactory> defaultDelegatingTypeConverters = getDefaultDelegatingConverterFactories();
        return createCompositeConverter(
                defaultBaseTypeConverters,
                defaultDelegatingTypeConverters);
    }
}
