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

package com.sabre.oss.conf4j.internal.factory;

import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.utils.KeyGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Holds sub-configurations in the generated classes for sub-configuration list properties.
 * This class is capable of creating configurations on demand and instantiating them using default values.
 */
public class SubConfigurationList extends AbstractUnmodifiableList<Object> {
    private final ConfigurationModel configurationModel;
    private final KeyGenerator keyGenerator;
    private final List<String> defaultPrefixes;
    private final ConfigurationInitializer configurationInitializer;
    private final List<Map<String, String>> defaultValues;
    private final Map<String, String> attributes;

    SubConfigurationList(ConfigurationModel configurationModel,
                         ConfigurationInitializer configurationInitializer, KeyGenerator keyGenerator,
                         List<String> defaultPrefixes, int size, List<Map<String, String>> defaultValues,
                         Map<String, String> attributes) {

        super(new ArrayList<>(size));

        this.configurationModel = configurationModel;
        this.configurationInitializer = configurationInitializer;
        this.defaultValues = defaultValues;
        this.keyGenerator = keyGenerator;
        this.defaultPrefixes = defaultPrefixes;
        this.attributes = attributes;

        for (int i = 0; i < size; i++) {
            target.add(createNewInstance(i));
        }
    }

    /**
     * Provides a list of (unmodifiable) configurations.
     * <p>
     * <b>Note:</b> Do not remove. It is used from the generated code.
     *
     * @param requiredSize specifies the resulting collection size. When it is {@code >} actual size, missing elements
     *                     will be created and initialized with default values.
     * @return unmodifiable list of configurations.
     * @throws IllegalArgumentException when {@code requiredSize} {@code <=} 0.
     */
    public List<Object> asUnmodifiableList(int requiredSize) {
        isTrue(requiredSize >= 0, "requiredSize must be >= 0");
        int currentSize = target.size();
        if (requiredSize < currentSize) {
            return target.subList(0, requiredSize);
        } else if (requiredSize == currentSize) {
            return this;
        } else {
            List<Object> items = new ArrayList<>(requiredSize);
            for (int i = 0; i < requiredSize; i++) {
                items.add((i < currentSize) ? target.get(i) : createNewInstance(i));
            }
            return unmodifiableList(items);
        }
    }

    private Object createNewInstance(int index) {
        KeyGenerator newKeyGenerator = createPrefix(index);

        return configurationInitializer.createSubConfiguration(configurationModel, newKeyGenerator, null, getDefaultValuesAt(index), attributes);
    }

    private Map<String, String> getDefaultValuesAt(int index) {
        return index < defaultValues.size() ? defaultValues.get(index) : emptyMap();
    }

    private KeyGenerator createPrefix(int index) {
        return keyGenerator.append(defaultPrefixes).appendIndex(index);
    }
}
