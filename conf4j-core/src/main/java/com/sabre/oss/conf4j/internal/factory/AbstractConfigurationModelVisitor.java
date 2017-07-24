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

package com.sabre.oss.conf4j.internal.factory;

import com.sabre.oss.conf4j.internal.model.*;

import static java.util.Objects.requireNonNull;

/**
 * Helper class which supports visiting {@link ConfigurationModel} and its properties.
 * <p>
 * Classes which process configuration may extend this class and implement {@code process...} methods
 * to perform required logic.
 * </p>
 */
public abstract class AbstractConfigurationModelVisitor {
    /**
     * Invoked at the beginning of processing.
     *
     * @param configurationModel configuration model.
     */
    protected abstract void processConfiguration(ConfigurationModel configurationModel);

    /**
     * Invoked for each value property.
     *
     * @param propertyModel property model.
     */
    protected abstract void processValueProperty(ValuePropertyModel propertyModel);

    /**
     * Invoked for each sub-configuration property.
     *
     * @param propertyModel property model.
     */
    protected abstract void processSubConfigurationProperty(SubConfigurationPropertyModel propertyModel);

    /**
     * Invoked for each sub-configuration list property.
     *
     * @param propertyModel property model.
     */
    protected abstract void processSubConfigurationListProperty(SubConfigurationListPropertyModel propertyModel);

    /**
     * Invokes {@link #processConfiguration(ConfigurationModel)} for configuration model and
     * {@link #processValueProperty(ValuePropertyModel)} for simple property,
     * {@link #processSubConfigurationProperty(SubConfigurationPropertyModel)} for sub-configuration property,
     * {@link #processSubConfigurationListProperty(SubConfigurationListPropertyModel)} for sub-configuration list property,
     *
     * @param configurationModel configuration model
     */
    protected void process(ConfigurationModel configurationModel) {
        requireNonNull(configurationModel, "configurationModel cannot be null");

        processConfiguration(configurationModel);
        for (PropertyModel propertyModel : configurationModel.getProperties()) {
            processProperty(propertyModel);
        }
    }

    protected void processProperty(PropertyModel propertyModel) {
        if (propertyModel instanceof ValuePropertyModel) {
            processValueProperty((ValuePropertyModel) propertyModel);
        } else if (propertyModel instanceof SubConfigurationPropertyModel) {
            processSubConfigurationProperty((SubConfigurationPropertyModel) propertyModel);
        } else if (propertyModel instanceof SubConfigurationListPropertyModel) {
            processSubConfigurationListProperty((SubConfigurationListPropertyModel) propertyModel);
        } else {
            throw new IllegalArgumentException("Unsupported property model " + propertyModel);
        }
    }
}
