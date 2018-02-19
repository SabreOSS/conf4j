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

package com.sabre.oss.conf4j.internal.model.provider.convention;

import com.sabre.oss.conf4j.internal.model.ConfigurationModelProvider;
import com.sabre.oss.conf4j.internal.model.provider.AbstractConfigurationModelProvider;

import static java.util.Arrays.asList;

/**
 * Extract configuration model from classes/interfaces based on convention. It is handy when it is not desirable
 * to pollute configuration classes with any <i>conf4j</i> specific api.
 * <p>
 * Convention doesn't allow specifying some metadata, for example it is not possible to specify more than one
 * <i>configuration key</i>, <i>fallback, global keys</i> nor <i>default values</i>.
 * In such cases it is possible to use <i>conf4j</i> annotations as a fallback.
 * <p>
 * Following conventions is used while extracting configuration data:
 * <ul>
 * <li>
 * A method associated with a configuration property must be public, abstract, returns non {@code void},
 * has no parameters and start with {@code get} (or {@code is} when methods return type is {@code boolean}).
 * </li>
 * <li>
 * Property name is used as configuration key for value properties or prefix for sub-configuration
 * and sub-configuration list properties.
 * </li>
 * <li>
 * A type is recognized as the configuration type if it is abstract type and all its abstract methods
 * are valid configuration methods as specified above.
 * </li>
 * <li>
 * A type is recognized as abstract configuration if it is a configuration type and its simple class name
 * starts with <i>Abstract</i> prefix.
 * </li>
 * <li>
 * Default values for value properties which returns primitive types (like {@code short}, {@code int})
 * are always defined and are same as default values for class fields of primitive types (usually {@code 0}).
 * For all other types, default value is not specified.
 * </li>
 * </ul>
 */
public class ConventionConfigurationModelProvider extends AbstractConfigurationModelProvider {
    private static final ConfigurationModelProvider instance = new ConventionConfigurationModelProvider();

    /**
     * Provides {@link ConfigurationModelProvider} instance.
     *
     * @return configuration model provider instance.
     */
    public static ConfigurationModelProvider getInstance() {
        return instance;
    }

    protected ConventionConfigurationModelProvider() {
        super(new ConventionMetadataExtractor());
        this.methodParsers = asList(
                new ConventionNonAbstractMethodParser(),
                new ConventionValuePropertyMethodParser(metadataExtractor),
                new ConventionSubConfigurationPropertyMethodParser(metadataExtractor, this),
                new ConventionSubConfigurationListPropertyMethodParser(metadataExtractor, this),
                new ConventionUnrecognizedPropertyMethodParser()
        );
    }

}
