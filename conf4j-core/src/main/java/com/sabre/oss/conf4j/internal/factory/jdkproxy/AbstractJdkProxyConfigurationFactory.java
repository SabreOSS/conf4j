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

package com.sabre.oss.conf4j.internal.factory.jdkproxy;

import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationFactory;
import com.sabre.oss.conf4j.internal.factory.ConfigurationInstanceCreator;
import com.sabre.oss.conf4j.source.ConfigurationValuesSource;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public abstract class AbstractJdkProxyConfigurationFactory extends AbstractConfigurationFactory {
    protected AbstractJdkProxyConfigurationFactory(ConfigurationInstanceCreator configurationInstanceCreator) {
        super(configurationInstanceCreator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createConfiguration(Class<T> configurationType, ConfigurationValuesSource valuesSource, ClassLoader classLoader) {
        requireNonNull(configurationType, "configurationType cannot be null");
        requireNonNull(valuesSource, "valuesSource cannot be null");

        if (!configurationType.isInterface()) {
            throw new IllegalArgumentException(
                    format("%s supports interfaces only, but configuration type %s is not an interface.",
                            getClass().getName(), configurationType.getName()));
        }

        return super.createConfiguration(configurationType, valuesSource, classLoader);
    }
}
