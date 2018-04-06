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

package com.sabre.oss.conf4j.internal.factory.javassist;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.config.ConfigurationValueProvider;
import com.sabre.oss.conf4j.internal.config.DynamicConfiguration;
import com.sabre.oss.conf4j.internal.config.PropertyMetadata;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import com.sabre.oss.conf4j.source.OptionalValue;
import javassist.*;

import java.lang.reflect.Type;

import static com.sabre.oss.conf4j.internal.Constants.METADATA_SUFFIX;

public class JavassistDynamicConfigurationInstanceCreator extends AbstractJavassistConfigurationInstanceCreator {
    @Override
    protected AbstractGenerator createGenerator(ConfigurationModel configurationModel, ClassLoader classLoader) {
        return new DynamicGenerator(configurationModel, classLoader);
    }

    protected static class DynamicGenerator extends AbstractGenerator {
        private static final String PARENT_CONFIGURATION_FIELD_NAME = "parentConfiguration";
        private static final String CONFIGURATION_SOURCE_FIELD_NAME = "configurationSource";
        private static final String TYPE_CONVERTER_FIELD_NAME = "typeConverter";
        private static final String CONFIGURATION_VALUE_PROVIDER_FIELD_NAME = "configurationValueProvider";

        DynamicGenerator(ConfigurationModel configurationModel, ClassLoader classLoader) {
            super(configurationModel, classLoader);
        }

        /*
         * Used by javassist. Do not remove.
         */
        @SuppressWarnings("unchecked")
        public static OptionalValue<Object> getConfigurationValue(Object configuration, PropertyMetadata metadata) {
            DynamicConfiguration dynamicConfiguration = (DynamicConfiguration) configuration;
            ConfigurationSource configurationSource = dynamicConfiguration.getConfigurationSource();
            TypeConverter<Object> typeConverter = (TypeConverter<Object>) dynamicConfiguration.getTypeConverter();
            ConfigurationValueProvider configurationValueProvider = dynamicConfiguration.getConfigurationValueProvider();
            return configurationValueProvider.getConfigurationValue(typeConverter, configurationSource, metadata);
        }

        @Override
        protected void processConfiguration(ConfigurationModel configurationModel) {
            super.processConfiguration(configurationModel);
            try {
                ctClass.addInterface(classPool.get(DynamicConfiguration.class.getName()));
                addFiledWithAccessors(PARENT_CONFIGURATION_FIELD_NAME, "getParentConfiguration", DynamicConfiguration.class, true);
                addConfigurationSourceFieldAndAccessors();
                addTypeConverterAccessors();
                addConfigurationValueProviderAccessors();
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        protected CtMethod addDynamicGetter(String propertyName, String methodName, CtField ctField, boolean internalOnly) throws CannotCompileException, NotFoundException {
            return addDynamicConfigurationPropertyGetter(propertyName, methodName, ctField);
        }

        @Override
        protected void addPropertyMetadata(String property) {
            try {
                String metaPropertyName = property + METADATA_SUFFIX;
                CtField ctField = addField(getPropertyValidJavaName(metaPropertyName), PropertyMetadata.class);
                addSetter(createSetterName(metaPropertyName), ctField, true);
            } catch (CannotCompileException | NotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        protected CtField addFiledWithAccessors(String fieldName, String methodName, Type fieldType, boolean internal) throws CannotCompileException, NotFoundException {
            CtField ctField = addField(fieldName, fieldType);

            addGetter(methodName, ctField, internal);
            addSetter(createSetterName(fieldName), ctField, internal);

            return ctField;
        }

        protected String cast(CtClass type, String variableName) {
            if (type instanceof CtPrimitiveType) {
                return unBox(variableName, (CtPrimitiveType) type);
            } else {
                return '(' + type.getName() + ") " + variableName;
            }
        }

        protected String unBox(String variableName, CtPrimitiveType type) {
            // generates i.e. ((java.lang.Boolean) o ).booleanValue()
            return "((" + type.getWrapperName() + ')' + variableName + ")." + type.getName() + "Value()";
        }

        private CtMethod addDynamicConfigurationPropertyGetter(String propertyName, String methodName, CtField ctField) throws CannotCompileException, NotFoundException {
            log.trace("Adding getter {}()", methodName);

            String body = new CodeBuilder()
                    .add("{")
                    .add("  $PropertyModel$ metadata = this.$metadataFieldName$;")
                    .add("  java.util.List keys = metadata.getKeySet();")
                    .add("  java.lang.reflect.Type type = metadata.getType();")
                    .add("  $OptionalValue$ value = $thisClass$.getConfigurationValue(this, metadata);")
                    .add("  return value.isPresent() ? $cast$ : this.$fieldName$;")
                    .add("}")
                    .var("PropertyModel", PropertyMetadata.class.getName())
                    .var("metadataFieldName", getPropertyValidJavaName(propertyName + METADATA_SUFFIX))
                    .var("thisClass", getClass().getName())
                    .var("fieldName", ctField.getName())
                    .var("OptionalValue", OptionalValue.class.getName())
                    .var("cast", cast(ctField.getType(), "value.get()"))
                    .code();

            CtMethod ctGetter = CtNewMethod.getter(methodName, ctField);
            ctGetter.setBody(body);
            ctClass.addMethod(ctGetter);

            return ctGetter;
        }

        private void addConfigurationSourceFieldAndAccessors() {
            try {
                CtField field = addField(CONFIGURATION_SOURCE_FIELD_NAME, ConfigurationSource.class);
                addSetter("setConfigurationSource", field, true);

                String getterName = "getConfigurationSource";
                log.trace("Adding getter {}()", getterName);
                String body = new CodeBuilder()
                        .add("{")
                        .add("  $ConfigurationSource$ source = this.$configurationSource$;")
                        .add("  if (source == null) {")
                        .add("    source = getParentConfiguration().getConfigurationSource();")
                        .add("  }")
                        .add("  return source;")
                        .add("}")
                        .var("ConfigurationSource", ConfigurationSource.class.getName())
                        .var("configurationSource", CONFIGURATION_SOURCE_FIELD_NAME)
                        .code();
                CtMethod ctGetter = CtNewMethod.getter(getterName, field);
                ctGetter.setBody(body);
                addInternalAnnotation(ctGetter);
                ctClass.addMethod(ctGetter);
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }

        private void addTypeConverterAccessors() {
            try {
                CtField field = addField(TYPE_CONVERTER_FIELD_NAME, TypeConverter.class);
                addSetter("setTypeConverter", field, true);

                String getterName = "getTypeConverter";
                log.trace("Adding getter {}()", getterName);
                String body = new CodeBuilder()
                        .add("{")
                        .add("  $TypeConverter$ typeConverter = this.$typeConverter$;")
                        .add("  if (typeConverter == null) {")
                        .add("    typeConverter = getParentConfiguration().getTypeConverter();")
                        .add("  }")
                        .add("  return typeConverter;")
                        .add("}")
                        .var("TypeConverter", TypeConverter.class.getName())
                        .var("typeConverter", TYPE_CONVERTER_FIELD_NAME)
                        .code();
                CtMethod ctGetter = CtNewMethod.getter(getterName, field);
                ctGetter.setBody(body);
                addInternalAnnotation(ctGetter);
                ctClass.addMethod(ctGetter);
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }

        private void addConfigurationValueProviderAccessors() {
            try {
                CtField field = addField(CONFIGURATION_VALUE_PROVIDER_FIELD_NAME, ConfigurationValueProvider.class);
                addSetter("setConfigurationValueProvider", field, true);

                String getterName = "getConfigurationValueProvider";
                log.trace("Adding getter {}()", getterName);
                String body = new CodeBuilder()
                        .add("{")
                        .add("  $ConfigurationValueProvider$ configurationValueProvider = this.$configurationValueProvider$;")
                        .add("  if (configurationValueProvider == null) {")
                        .add("    configurationValueProvider = getParentConfiguration().getConfigurationValueProvider();")
                        .add("  }")
                        .add("  return configurationValueProvider;")
                        .add("}")
                        .var("ConfigurationValueProvider", ConfigurationValueProvider.class.getName())
                        .var("configurationValueProvider", CONFIGURATION_VALUE_PROVIDER_FIELD_NAME)
                        .code();
                CtMethod ctGetter = CtNewMethod.getter(getterName, field);
                ctGetter.setBody(body);
                addInternalAnnotation(ctGetter);
                ctClass.addMethod(ctGetter);
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
