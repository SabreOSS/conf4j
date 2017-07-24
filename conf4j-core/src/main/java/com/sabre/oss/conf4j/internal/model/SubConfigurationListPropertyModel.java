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

package com.sabre.oss.conf4j.internal.model;

import com.sabre.oss.conf4j.source.Attributes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.internal.Constants.COLLECTION_SIZE_SUFFIX;
import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.getSizeKey;
import static com.sabre.oss.conf4j.source.OptionalValue.present;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public class SubConfigurationListPropertyModel extends PropertyModel {
    private final ConfigurationModel itemTypeModel;
    private final List<String> prefixes;
    private final boolean resetPrefix;
    private final int defaultSize;
    private final List<Map<String, String>> defaultValues;
    private final List<String> sizeKeys;
    private final ValuePropertyModel sizePropertyModel;

    public SubConfigurationListPropertyModel(
            String propertyName, Class<?> type, Method method, ConfigurationModel itemTypeModel, String description,
            List<String> prefixes, boolean resetPrefix, int defaultSize, List<Map<String, String>> defaultValues,
            Attributes customAttributes) {

        super(propertyName, type, method, description, customAttributes);
        this.itemTypeModel = requireNonNull(itemTypeModel, "itemTypeModel cannot be null");
        this.prefixes = requireNonNull(prefixes, "prefixes cannot be null");
        this.resetPrefix = resetPrefix;
        this.defaultSize = defaultSize;
        this.defaultValues = requireNonNull(defaultValues, "defaultValues cannot be null");
        this.sizeKeys = calcSizeKeys();
        this.sizePropertyModel = createSizePropertyModel(propertyName, method, resetPrefix, defaultSize);
    }

    @Override
    public Class<?> getType() {
        return (Class<?>) super.getType();
    }

    public ConfigurationModel getItemTypeModel() {
        return itemTypeModel;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public boolean isResetPrefix() {
        return resetPrefix;
    }

    public int getDefaultSize() {
        return defaultSize;
    }

    public List<Map<String, String>> getDefaultValues() {
        return defaultValues;
    }

    public List<String> getSizeKeys() {
        return sizeKeys;
    }

    public ValuePropertyModel getSizePropertyModel() {
        return sizePropertyModel;
    }

    private List<String> calcSizeKeys() {
        if (prefixes.isEmpty()) {
            return singletonList(getSizeKey(null));
        } else {
            List<String> configurationKeys = new ArrayList<>(prefixes.size());
            for (String prefix : prefixes) {
                configurationKeys.add(getSizeKey(prefix));
            }
            return unmodifiableList(configurationKeys);
        }
    }

    private ValuePropertyModel createSizePropertyModel(String propertyName, Method method, boolean resetPrefix, int defaultSize) {
        return new ValuePropertyModel(
                propertyName + COLLECTION_SIZE_SUFFIX, Integer.TYPE, Integer.TYPE,
                method, // todo - this is wrong, the method for the size doesn't exist, but null is not allowed
                null, sizeKeys, null, resetPrefix, present(Integer.toString(defaultSize)),
                null, null, customAttributes);
    }

}

