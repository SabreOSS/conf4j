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

package com.sabre.oss.conf4j.converter.xml;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.converter.xml.JaxbPool.Handle;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Type converter which supports converts object to/from xml. The object class must be properly annotated
 * with JAXB annotations. Only classes which has {@link XmlRootElement} applied are supported.
 *
 * @see XmlRootElement
 */
public class JaxbTypeConverter<T> implements TypeConverter<T> {

    // Since there is only one instance of this object created, we want to be sure it is capable of handling all kinds of
    // Jaxb objects used in conf4j classes; thus there will be a separate JaxbPool for each distinct Jaxb class.
    private final ConcurrentMap<Class<T>, JaxbPool> jaxbPoolMap = new ConcurrentHashMap<>();

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class && ((Class<?>) type).getAnnotation(XmlRootElement.class) != null;
    }

    @Override
    public T fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (isBlank(value)) {
            return null;
        }

        Class<T> targetClazz = (Class<T>) type;
        JaxbPool jaxbPool = getJaxbPool(targetClazz);
        try (Handle<Unmarshaller> handle = jaxbPool.borrowUnmarshaller()) {
            return targetClazz.cast(handle.get().unmarshal(new ByteArrayInputStream(value.getBytes())));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Unable to convert xml to  " + targetClazz + " using jaxb", e);
        }
    }

    @Override
    public String toString(Type type, T value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        JaxbPool jaxbPool = getJaxbPool((Class<T>) type);
        try (Handle<Marshaller> handle = jaxbPool.borrowMarshaller()) {
            Marshaller marshaller = handle.get();
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(value, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Unable to convert " + value.getClass().getName() + " to xml using jaxb", e);
        }
    }

    private JaxbPool getJaxbPool(Class<T> clazz) {
        return jaxbPoolMap.computeIfAbsent(clazz, JaxbPool::new);
    }
}
