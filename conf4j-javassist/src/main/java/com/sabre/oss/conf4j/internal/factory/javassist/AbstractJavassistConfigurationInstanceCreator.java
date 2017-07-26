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

package com.sabre.oss.conf4j.internal.factory.javassist;

import com.sabre.oss.conf4j.annotation.Internal;
import com.sabre.oss.conf4j.internal.factory.AbstractConfigurationModelVisitor;
import com.sabre.oss.conf4j.internal.factory.ConfigurationInstanceCreator;
import com.sabre.oss.conf4j.internal.factory.SubConfigurationList;
import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.model.SubConfigurationListPropertyModel;
import com.sabre.oss.conf4j.internal.model.SubConfigurationPropertyModel;
import com.sabre.oss.conf4j.internal.model.ValuePropertyModel;
import com.sabre.oss.conf4j.internal.utils.ReflectionUtils;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static com.sabre.oss.conf4j.internal.Constants.*;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.substringBefore;

abstract class AbstractJavassistConfigurationInstanceCreator implements ConfigurationInstanceCreator {
    protected abstract AbstractGenerator createGenerator(ConfigurationModel configurationModel, ClassLoader classLoader);

    @Override
    public <T> T createInstance(ConfigurationModel configurationModel, ClassLoader classLoader) {
        Class<T> generatedClass = generateClass(configurationModel, classLoader);
        return ReflectionUtils.createInstance(generatedClass);
    }

    protected <T> Class<T> generateClass(ConfigurationModel configurationModel, ClassLoader classLoader) {
        return createGenerator(configurationModel, classLoader).generateClass();
    }

    public static String getPropertyValidJavaName(String propertyName) {
        requireNonNull(propertyName, "propertyName cannot be null");

        return SourceVersion.isName(propertyName) ? propertyName : "__" + propertyName;
    }

    protected abstract static class AbstractGenerator extends AbstractConfigurationModelVisitor {
        private static final CtClass[] EMPTY = new CtClass[0];

        protected final Logger log = LoggerFactory.getLogger(getClass());

        protected final ConfigurationModel configurationModel;
        protected final ClassLoader classLoader;
        protected final Class<?> configurationClass;
        protected final String className;
        protected final ClassPool classPool;
        protected CtClass ctClass;

        protected AbstractGenerator(ConfigurationModel configurationModel, ClassLoader classLoader) {
            this.configurationModel = requireNonNull(configurationModel, "configurationModel cannot be null");
            this.classLoader = requireNonNull(classLoader, "classLoader cannot be null");
            this.configurationClass = configurationModel.getConfigurationType();
            this.className = createClassName(configurationClass) + "$Javassist" + this.getClass().getSimpleName();
            this.classPool = new ClassPool(true);
            this.classPool.appendClassPath(new LoaderClassPath(classLoader));
        }

        public synchronized <T> Class<T> generateClass() {
            try {
                @SuppressWarnings("unchecked")
                Class<T> clazz = (Class<T>) classLoader.loadClass(className);
                return clazz;
            } catch (ClassNotFoundException ignore) {
            }

            log.debug("Generating implementation class for configuration type {}", configurationClass.getName());

            process(configurationModel);

            return generateClass(classLoader);
        }

        @Override
        protected void processConfiguration(ConfigurationModel configurationModel) {
            this.ctClass = classPool.makeClass(className);
            this.ctClass.getClassFile().setVersionToJava5();

            try {
                addSuperclassOrInterface();
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void processValueProperty(ValuePropertyModel propertyModel) {
            String propertyName = propertyModel.getPropertyName();
            addPropertyMetadata(propertyName);
            Type type = propertyModel.getType().equals(propertyModel.getDeclaredType()) ? propertyModel.getType() : propertyModel.getDeclaredType();
            addPropertyFieldAndAccessors(propertyName, propertyModel.getMethod().getName(), type, true, false);
        }

        @Override
        protected void processSubConfigurationProperty(SubConfigurationPropertyModel propertyModel) {
            String propertyName = propertyModel.getPropertyName();
            Class<?> type = propertyModel.getType();
            if (type.equals(propertyModel.getDeclaredType())) {
                addPropertyFieldAndAccessors(propertyName, createGetterName(propertyName), type, false, false);
            } else {
                try {
                    CtField field = addField(getPropertyValidJavaName(propertyName), type);
                    addSetter(createSetterName(propertyName), field, false);
                    CtClass returnType = classPool.get(propertyModel.getDeclaredType().getName());
                    String body = "return this." + field.getName() + ';';
                    CtMethod method = CtNewMethod.make(returnType, createGetterName(propertyName), EMPTY, EMPTY, body, ctClass);
                    ctClass.addMethod(method);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        protected void processSubConfigurationListProperty(SubConfigurationListPropertyModel propertyModel) {
            processValueProperty(propertyModel.getSizePropertyModel());
            String propertyName = propertyModel.getPropertyName();
            try {
                String configurationListSize = propertyName + COLLECTION_SIZE_SUFFIX;
                CtField listSizeField = ctClass.getField(configurationListSize);
                addDynamicGetter(configurationListSize, createGetterName(configurationListSize), listSizeField, true);
                addListOfConfigurationsGetter(propertyName, createGetterName(propertyName));
            } catch (Exception e) {
                throw new RuntimeException("subConfigurationList " + propertyName + " cannot be created", e);
            }
        }

        @SuppressWarnings("unchecked")
        protected <T> Class<T> generateClass(ClassLoader classLoader) {
            try {
                return ctClass.toClass(classLoader, getClass().getProtectionDomain());
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }

        protected void addInternalAnnotation(CtMethod ctMethod) {
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            attr.setAnnotations(new Annotation[]{new Annotation(Internal.class.getName(), constPool)});
            ctMethod.getMethodInfo().addAttribute(attr);
        }

        protected void addPropertyMetadata(String property) {
        }

        protected CtField addField(String fieldName, Type fieldType) throws CannotCompileException, NotFoundException {
            Class<?> actualPropertyType = (Class<?>) fieldType;
            log.trace("Adding field {} {}", actualPropertyType.getName(), fieldName);
            CtClass ctFieldType = classPool.get(actualPropertyType.getName());
            CtField field = new CtField(ctFieldType, fieldName, ctClass);
            ctClass.addField(field);

            return field;
        }

        protected CtField addPropertyField(String fieldName, Type type) throws CannotCompileException, NotFoundException {
            Class<?> rawType = type instanceof ParameterizedType ?
                    (Class<?>) ((ParameterizedType) type).getRawType() :
                    (Class<?>) type;

            return addField(fieldName, rawType);
        }

        protected CtField addPropertyFieldAndAccessors(String propertyName, String methodName, Type propertyType, boolean dynamic, boolean internal) {
            try {
                String configurationFieldName = getPropertyValidJavaName(propertyName);
                CtField ctField = addPropertyField(configurationFieldName, propertyType);
                if (dynamic) {
                    addDynamicGetter(propertyName, methodName, ctField, internal);
                } else {
                    addGetter(methodName, ctField, internal);
                }

                try {
                    ctClass.getDeclaredMethod(createSetterName(propertyName));
                } catch (NotFoundException ignore) {
                    addSetter(createSetterName(propertyName), ctField, internal);
                }

                return ctField;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected CtMethod addGetter(String methodName, CtField ctField, boolean internalOnly) throws CannotCompileException, NotFoundException {
            log.trace("Adding getter {}()", methodName);
            String retClassName;
            if (ctField.getType().getName().endsWith(GENERATED_CLASS_NAME_SUFFIX)) {
                retClassName = substringBefore(ctField.getType().getName(), GENERATED_CLASS_NAME_SUFFIX);
            } else {
                retClassName = ctField.getType().getName();
            }

            String body = new CodeBuilder()
                    .add("public $returnType$ $methodName$() {")
                    .add(" return this.$fieldName$;")
                    .add("}")
                    .var("returnType", retClassName)
                    .var("methodName", methodName)
                    .var("fieldName", ctField.getName())
                    .code();

            CtMethod method = CtNewMethod.make(body, ctClass);
            if (internalOnly) {
                addInternalAnnotation(method);
            }
            ctClass.addMethod(method);

            return method;
        }

        protected CtMethod addDynamicGetter(String propertyName, String methodName, CtField ctField, boolean internalOnly) throws CannotCompileException, NotFoundException {
            return addGetter(methodName, ctField, internalOnly);
        }

        protected void addSetter(String methodName, CtField field, boolean internal) throws CannotCompileException {
            log.trace("Adding setter {}()", methodName);
            CtMethod ctSetter = CtNewMethod.setter(methodName, field);
            if (internal) {
                addInternalAnnotation(ctSetter);
            }
            ctClass.addMethod(ctSetter);
        }

        protected String createSetterName(String propertyName) {
            return "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }

        protected String createGetterName(String propertyName) {
            return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }

        private void addSuperclassOrInterface() throws NotFoundException, CannotCompileException {
            if (configurationClass.isInterface()) {
                log.trace("Class implements configuration interface {}", configurationClass.getSimpleName());
                ctClass.setInterfaces(new CtClass[]{
                        classPool.get(configurationClass.getName()),
                        classPool.get(Serializable.class.getName())
                });
            } else {
                log.trace("Class extends abstract configuration class {}", configurationClass.getSimpleName());
                ctClass.setSuperclass(classPool.get(configurationClass.getName()));
                ctClass.setInterfaces(new CtClass[]{classPool.get(Serializable.class.getName())});
            }
        }

        private void addListOfConfigurationsGetter(String propertyName, String methodName) throws CannotCompileException, NotFoundException {
            log.trace("Adding getter {}()", methodName);

            String listPropertyName = propertyName + LIST_SUFFIX;

            // add field and accessors for internal list of configurations
            CtField listField = addField(getPropertyValidJavaName(listPropertyName), List.class);
            addGetter(methodName + LIST_SUFFIX, listField, true);
            addSetter(createSetterName(listPropertyName), listField, true);

            String body = new CodeBuilder()
                    .add("public java.util.List $methodName$() {")
                    .add("  int itemsSize = this.$collectionSizeMethodName$();")
                    .add("  return (($subConfigurationListHolderClass$) this.$listFieldName$).asUnmodifiableList(itemsSize);")
                    .add("}")
                    .var("methodName", methodName)
                    .var("collectionSizeMethodName", createGetterName(propertyName + COLLECTION_SIZE_SUFFIX))
                    .var("subConfigurationListHolderClass", SubConfigurationList.class.getName())
                    .var("listFieldName", getPropertyValidJavaName(listPropertyName))
                    .code();
            CtMethod method = CtNewMethod.make(body, ctClass);
            addInternalAnnotation(method);
            ctClass.addMethod(method);
        }

        private String createClassName(Class<?> targetClass) {
            String name = targetClass.getCanonicalName();
            return name.endsWith(GENERATED_CLASS_NAME_SUFFIX) ? name : name + GENERATED_CLASS_NAME_SUFFIX;
        }
    }
}
