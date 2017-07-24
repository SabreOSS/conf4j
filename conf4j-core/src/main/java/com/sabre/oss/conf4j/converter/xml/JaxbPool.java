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
package com.sabre.oss.conf4j.converter.xml;


import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.lang.Thread.currentThread;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.Validate.notNull;

class JaxbPool {
    public interface Handle<T> extends AutoCloseable {

        T get();

        @Override
        void close();
    }

    private final JAXBContext context;
    private final Schema schema;

    private final Pool<Marshaller> marshallers = new Pool<>(new MarshallerSupplier());
    private final Pool<Unmarshaller> unmarshallers = new Pool<>(new UnmarshallerSupplier());

    JaxbPool(Class<?> clazz) {
        try {
            this.context = JAXBContext.newInstance(clazz);
            this.schema = readXsdSchema(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Schema readXsdSchema(Class<?> clazz) throws IOException, SAXException {
        XmlSchema xmlSchema = clazz.getPackage().getAnnotation(XmlSchema.class);
        String schemaLocation = xmlSchema != null ? xmlSchema.location() : null;
        if (schemaLocation == null) {
            return null;
        }
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        return schemaFactory.newSchema(getResource(schemaLocation));
    }

    public JAXBContext getContext() {
        return context;
    }

    public Handle<Marshaller> borrowMarshaller() {
        return new HandleImpl<>(marshallers);
    }

    public Handle<Unmarshaller> borrowUnmarshaller() {
        return new HandleImpl<>(unmarshallers);
    }

    private static class HandleImpl<E> implements Handle<E> {

        private final Pool<E> pool;
        private final AtomicReference<E> element = new AtomicReference<>();

        private HandleImpl(Pool<E> pool) {
            this.pool = pool;
        }

        @Override
        public E get() {
            E e = pool.borrow();
            element.set(e);
            return e;
        }

        @Override
        public void close() {
            E e = element.getAndSet(null);
            if (e != null) {
                pool.release(e);
            }
        }
    }

    private static class Pool<E> {
        private final Supplier<E> supplier;
        private final Deque<WeakReference<E>> elements = new ArrayDeque<>();

        private Pool(Supplier<E> supplier) {
            this.supplier = notNull(supplier, "supplier cannot be null");
        }

        public E borrow() {
            E element = null;
            synchronized (elements) {
                while (!elements.isEmpty()) {
                    WeakReference<E> reference = elements.removeLast();
                    element = reference.get();
                    if (element != null) {
                        break;
                    }
                }
            }
            if (element == null) {
                element = supplier.get();
            }
            return element;
        }

        public void release(E element) {
            notNull(element, "element cannot be null");

            synchronized (elements) {
                elements.addLast(new WeakReference<>(element));
            }
        }
    }

    private class MarshallerSupplier implements Supplier<Marshaller> {
        @Override
        public Marshaller get() {
            try {
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.setSchema(schema);
                return marshaller;
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class UnmarshallerSupplier implements Supplier<Unmarshaller> {
        @Override
        public Unmarshaller get() {
            try {
                Unmarshaller unmarshaller = context.createUnmarshaller();
                unmarshaller.setSchema(schema);
                return unmarshaller;
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static URL getResource(String resourceName) {
        ClassLoader loader = defaultIfNull(currentThread().getContextClassLoader(), JaxbPool.class.getClassLoader());
        URL url = loader.getResource(resourceName);
        if (url == null) {
            throw new IllegalArgumentException("resource " + resourceName + " not found.");
        }
        return url;
    }
}
