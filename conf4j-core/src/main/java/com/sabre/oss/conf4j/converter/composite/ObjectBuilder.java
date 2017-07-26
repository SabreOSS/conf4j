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

package com.sabre.oss.conf4j.converter.composite;

import java.util.*;

import static java.lang.String.format;

/**
 * {@link ObjectBuilder} is a builder that build object's hierarchy
 * based on {@link Map}s and {@link List}s.
 */
class ObjectBuilder {

    private final Deque<Object> stack = new LinkedList<>();

    Object build() {
        if (stack.size() != 1) {
            throw new IllegalStateException("Current pointer does not point to the top level object.");
        }
        return stack.pop();
    }

    ObjectBuilder addKey(Object value) {
        Object current = stack.peek();
        if (!(current instanceof Map)) {
            throw new IllegalStateException(format("Can't write key in context of %s. Key can be only added to Map.", getType(current.getClass())));
        }
        stack.push(new KeyWrapper(value));
        return this;
    }

    ObjectBuilder addValue(Object object) {
        return appendValue(object, false);
    }

    ObjectBuilder beginMap() {
        return beginMap(new LinkedHashMap<>());
    }

    ObjectBuilder beginMap(Map<?, ?> map) {
        return appendValue(map, true);
    }

    ObjectBuilder endMap() {
        return end(Map.class);
    }

    ObjectBuilder beginList() {
        return beginList(new ArrayList<>());
    }

    ObjectBuilder beginList(List<?> collectionItemsHolder) {
        return appendValue(collectionItemsHolder, true);
    }

    ObjectBuilder endList() {
        return end(List.class);
    }

    private ObjectBuilder end(Class<?> type) {
        String typeName = type.getSimpleName();
        if (stack.isEmpty()) {
            throw new IllegalStateException("Requested ending " + typeName + " but no objects were pushed to builder");
        }
        if (stack.size() == 1) {
            throw new IllegalStateException("Requested ending top level " + typeName);
        }
        Object current = stack.peek();
        if (!type.isInstance(current)) {
            throw new IllegalStateException(format("Requested ending %s but current type is %s", typeName, getType(current)));
        }
        stack.pop();
        return this;
    }

    @SuppressWarnings("unchecked")
    private ObjectBuilder appendValue(Object object, boolean editable) {
        Object current = stack.peek();
        if (stack.isEmpty()) {
            stack.push(object);
        } else if (current instanceof List) {
            ((List<Object>) current).add(object);
        } else if (current instanceof KeyWrapper) {
            stack.pop();
            Object key = ((KeyWrapper) current).getKey();
            Map<Object, Object> map = (Map<Object, Object>) stack.peek();
            map.put(key, object);
        } else {
            throw new IllegalStateException(format("Can't add %s to %s", object, getType(current)));
        }
        if (editable && (object instanceof List || object instanceof Map)) {
            stack.push(object);
        }
        return this;
    }

    private String getType(Object object) {
        if (object instanceof Map) {
            return "Map";
        } else if (object instanceof List) {
            return "List";
        }
        return Objects.isNull(object) ? "null" : object.toString();
    }

    private static class KeyWrapper {

        private final Object key;

        KeyWrapper(Object key) {
            this.key = key;
        }

        Object getKey() {
            return key;
        }
    }
}
