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

package com.sabre.oss.conf4j.internal.factory;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.config.ConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.config.PropertyMetadata;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.model.SubConfigurationListPropertyModel;
import com.sabre.oss.conf4j.internal.model.SubConfigurationPropertyModel;
import com.sabre.oss.conf4j.internal.model.ValuePropertyModel;
import com.sabre.oss.conf4j.internal.utils.KeyGenerator;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import com.sabre.oss.conf4j.source.OptionalValue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.sabre.oss.conf4j.internal.utils.AttributesUtils.mergeAttributes;
import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.emptyKeyGenerator;
import static com.sabre.oss.conf4j.internal.utils.KeyGenerator.keyGenerator;
import static com.sabre.oss.conf4j.source.OptionalValue.present;

public abstract class AbstractConfigurationInitializer extends AbstractConfigurationModelVisitor implements ConfigurationInitializer {
    protected final Object configuration;
    protected final ConfigurationModel configurationModel;
    protected final ClassLoader classLoader;
    protected final ConfigurationInstanceCreator configurationInstanceCreator;
    protected final TypeConverter<?> typeConverter;
    protected final ConfigurationSource configurationSource;
    protected final String fallbackKeyPrefix;
    protected final Map<String, String> defaultValues;
    protected final Map<String, String> attributes;
    protected final ConfigurationValueProvider configurationValueProvider;

    protected KeyGenerator keyGenerator;

    protected AbstractConfigurationInitializer(
            Object configuration,
            ConfigurationModel configurationModel,
            ClassLoader classLoader,
            ConfigurationInstanceCreator configurationInstanceCreator,
            TypeConverter<?> typeConverter,
            ConfigurationSource configurationSource,
            KeyGenerator keyGenerator,
            String fallbackKeyPrefix,
            Map<String, String> defaultValues,
            Map<String, String> attributes,
            ConfigurationValueProvider configurationValueProvider) {

        this.configuration = configuration;
        this.configurationModel = configurationModel;
        this.classLoader = classLoader;
        this.configurationInstanceCreator = configurationInstanceCreator;
        this.typeConverter = typeConverter;
        this.configurationSource = configurationSource;
        this.keyGenerator = keyGenerator;
        this.fallbackKeyPrefix = fallbackKeyPrefix;
        this.defaultValues = defaultValues;
        this.attributes = attributes;
        this.configurationValueProvider = configurationValueProvider;
    }

    @Override
    public void initializeConfiguration() {
        process(configurationModel);
    }

    @Override
    public Object createSubConfiguration(
            ConfigurationModel subConfigurationModel, KeyGenerator keyGenerator, String fallbackKey,
            Map<String, String> defaultValues, Map<String, String> attributes) {

        Object subConfiguration = configurationInstanceCreator.createInstance(subConfigurationModel, classLoader);

        ConfigurationInitializer subConfigurationInitializer = createSubConfigurationInitializer(
                subConfiguration, subConfigurationModel, keyGenerator, fallbackKey, defaultValues, attributes);
        subConfigurationInitializer.initializeConfiguration();

        return subConfiguration;
    }

    protected abstract ConfigurationInitializer createSubConfigurationInitializer(
            Object subConfiguration, ConfigurationModel configurationModel, KeyGenerator keyGenerator,
            String fallbackKey, Map<String, String> defaultValues, Map<String, String> attributes);

    protected abstract ConfigurationPropertiesAccessor getConfigurationPropertiesAccessor();

    protected abstract void storePropertyMetadata(PropertyMetadata propertyMetadata);

    @Override
    protected void processConfiguration(ConfigurationModel configurationModel) {
        this.keyGenerator = this.keyGenerator.append(configurationModel.getPrefixes());
    }

    @Override
    protected void processValueProperty(ValuePropertyModel propertyModel) {
        String propertyName = propertyModel.getPropertyName();
        Type type = propertyModel.getType();
        List<String> keySet = KeySetUtils.keySet(
                propertyModel.isResetPrefix() ? emptyKeyGenerator() : keyGenerator, propertyModel.getEffectiveKey(),
                fallbackKeyPrefix == null ? null : keyGenerator(fallbackKeyPrefix),
                propertyModel.getFallbackKey());

        OptionalValue<String> defaultValue = defaultValues.containsKey(propertyName) ? present(defaultValues.get(propertyName)) : propertyModel.getDefaultValue();
        String encryptionProvider = propertyModel.getEncryptionProviderName();
        Class<TypeConverter<?>> typeConverterClass = propertyModel.getTypeConverterClass();

        Map<String, String> propertyAttributes = mergeAttributes(attributes, propertyModel.getAttributes());
        PropertyMetadata propertyMetadata = new PropertyMetadata(propertyName, type, typeConverterClass, keySet, defaultValue, encryptionProvider, propertyAttributes);
        storePropertyMetadata(propertyMetadata);
    }

    @Override
    protected void processSubConfigurationProperty(SubConfigurationPropertyModel propertyModel) {
        String propertyName = propertyModel.getPropertyName();

        KeyGenerator subConfigurationKeyGenerator = propertyModel.isResetPrefix() ?
                keyGenerator(propertyModel.getPrefixes()) :
                this.keyGenerator.append(propertyModel.getPrefixes());

        Map<String, String> propertyAttributes = mergeAttributes(attributes, propertyModel.getAttributes());
        Object subConfiguration = createSubConfiguration(propertyModel.getTypeModel(),
                subConfigurationKeyGenerator, propertyModel.getFallbackKey(), propertyModel.getDefaultValues(), propertyAttributes);

        getConfigurationPropertiesAccessor().setSubConfigurationProperty(propertyName, subConfiguration);
    }

    @Override
    protected void processSubConfigurationListProperty(SubConfigurationListPropertyModel propertyModel) {
        processValueProperty(propertyModel.getSizePropertyModel());

        ConfigurationPropertiesAccessor configurationPropertiesAccessor = getConfigurationPropertiesAccessor();
        String propertyName = propertyModel.getPropertyName();
        SubConfigurationList list = configurationPropertiesAccessor.getSubConfigurationListProperty(propertyName);
        if (list == null) {
            Map<String, String> propertyAttributes = mergeAttributes(attributes, propertyModel.getAttributes());
            list = new SubConfigurationList(
                    propertyModel.getItemTypeModel(), this,
                    propertyModel.isResetPrefix() ? emptyKeyGenerator() : this.keyGenerator,
                    propertyModel.getPrefixes(),
                    propertyModel.getDefaultSize(),
                    propertyModel.getDefaultValues(),
                    propertyAttributes);
            configurationPropertiesAccessor.setSubConfigurationListProperty(propertyName, list);
        }
    }
}
