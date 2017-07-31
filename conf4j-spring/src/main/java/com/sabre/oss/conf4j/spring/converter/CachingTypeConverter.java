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

import com.sabre.oss.conf4j.converter.TypeConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.util.Assert;

import java.lang.reflect.Type;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.Assert.hasText;

/**
 * Caching type converter which uses SpringFramework cache abstraction for caching values converted from string.
 * <p>
 * <b>Note:</b>Caching is enabled only for {@link #fromString(Type, String, Map)} method. Converting a value to String
 * by {@link #toString(Type, Object, Map)} (which in most cases is not used frequently) doesn't take advantage of caching
 * (but it may change in the future). The same applies to {@link #isApplicable(Type, Map)} which is usually very fast
 * and caching it won't provide any performance improvement.
 * </p>
 * <p>
 * {@code CachingTypeConverter} delegates to {@link #setTypeConverter(TypeConverter) typeConverter}
 * and the resulting value is stored in the cache. Because the value may be reused it should be <i>immutable</i>
 * to avoid unintended cache modifications.
 * </p>
 * <p>
 * Before this converter can be used {@link #setTypeConverter(TypeConverter) typeConverter}
 * and {@link #setCacheManager(CacheManager) cacheManager} must be set.
 * </p>
 * <p>
 * <b>Note:</b>
 * Use this converter if deemed it will improve overall performance. Type converters are usually extremely fast;
 * therefore, caching may slow down conversions. This converter is useful when the conversion is done frequently
 * for complex types like {@code Map<String, List<String>>} but be aware it is associated with higher memory utilization
 * (both string representation and value are cached).
 * </p>
 *
 * @see TypeConverter
 * @see CacheManager
 * @see Cache
 */
public class CachingTypeConverter<T> implements TypeConverter<T>, InitializingBean {
    private TypeConverter<T> typeConverter;
    private CacheManager cacheManager;
    private String cacheName = "conf4j.typeConverterCache";
    private Cache cache;

    /**
     * Returns the type converter which this class delegates to perform conversions.
     *
     * @return type converter.
     */
    public TypeConverter<?> getTypeConverter() {
        return typeConverter;
    }

    /**
     * Set the type converter which will be used for performing conversions.
     *
     * @param typeConverter type converter.
     * @throws NullPointerException when {@code typeConverter} is {@code null}.
     */
    public void setTypeConverter(TypeConverter<T> typeConverter) {
        this.typeConverter = requireNonNull(typeConverter, "typeConverter cannot be null");
    }

    /**
     * Returns {@link CacheManager} used by caching conversion result.
     *
     * @return cache manager.
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Sets the cache manager which will be used for creating a cache.
     *
     * @param cacheManager cache manager.
     * @throws NullPointerException when {@code cacheManager} is {@code null}.
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Provides cache region name which is used for caching converted values.
     *
     * @return cache region name.
     * @see CacheManager#getCache(String)
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Sets the cache region name which is used for caching converted values.
     *
     * @param cacheName cache region name.
     * @throws IllegalArgumentException when {@code cacheName} is {@code null} or blank.
     */
    public void setCacheName(String cacheName) {
        hasText(cacheName, "cacheName cannot be blank");
        this.cacheName = cacheName;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.state(getTypeConverter() != null, "typeConverter property is required");
        Assert.state(getCacheManager() != null, "cacheManager property is required");

        cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("Cache " + cacheName + " is not available in the cache manager");
        }
    }

    /**
     * Check if the type converter is applicable for type {@code type}.
     * This method delegates to {@link #typeConverter} without caching because
     * {@link TypeConverter#isApplicable(Type, Map)} is usually much faster than accessing the cache.
     *
     * @param type actual type definition.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     * @return {@code true} when this type converter is applicable for a given type definition.
     * @throws NullPointerException when {@code type} is {@code null}.
     */
    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        /*
         * This method is not cached because {@link TypeConverter#isApplicable(Type, Map<String, String>)}
         * is usually much faster than accessing the cache.
         */
        return typeConverter.isApplicable(type, attributes);
    }

    /**
     * Converts String to the target type.
     * This method checks whether the cache contains the converted value, and if not, obtains it from {@link #typeConverter},
     * stores conversion result in the cache and provides the result.
     *
     * @param type  actual type definition.
     * @param value string representation of the value which is converted to {@code T}.
     *              In case it is {@code null}, converter should return either {@code null} or a value
     *              that is equivalent e.g. an empty list.
     * @return value converted to type {@code T}.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     * @throws IllegalArgumentException when {@code value} cannot be converted to {@code T}.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public T fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        SimpleKey key = new SimpleKey(type, attributes, value);
        ValueWrapper valueWrapper = cache.get(key);

        if (valueWrapper != null) {
            return (T) valueWrapper.get();
        } else {
            T val = typeConverter.fromString(type, value, attributes);
            valueWrapper = cache.putIfAbsent(key, val);
            if (valueWrapper == null) {
                return val;
            } else {
                return (T) valueWrapper.get();
            }
        }
    }

    /**
     * Converts value from target type to String.
     * This method delegates to {@link #typeConverter} without caching because
     * {@link TypeConverter#toString(Type, Object, Map)} )} is usually much faster than accessing the cache.
     *
     * @param type  actual type definition.
     * @param value value that needs to be converted to string.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     * @return string representation of the {@code value}.
     * @throws IllegalArgumentException {@code value} cannot be converted to string.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public String toString(Type type, T value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        /*
         * This method is not cached because {@link TypeConverter#isApplicable(Type, Map<String, String>)}
         * is usually much faster than accessing the cache.
         */
        return typeConverter.toString(type, value, attributes);
    }
}
