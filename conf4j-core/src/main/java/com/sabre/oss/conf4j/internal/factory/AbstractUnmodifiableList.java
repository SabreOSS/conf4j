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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * This is a base class for immutable lists which delegates to the actual implementation.
 *
 * @param <E> the type of elements in this list
 */
abstract class AbstractUnmodifiableList<E> implements List<E>, RandomAccess {
    protected final List<E> target;

    AbstractUnmodifiableList(List<E> target) {
        this.target = requireNonNull(target, "target cannot be null");
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return target.contains(o);
    }

    @Override
    public Object[] toArray() {
        return target.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return target.toArray(a);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return target.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public E get(int index) {
        return target.get(index);
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public int indexOf(Object o) {
        return target.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return target.lastIndexOf(o);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIterator<>(target.iterator());
    }

    @Override
    public ListIterator<E> listIterator() {
        return new UnmodifiableListIterator<>(target.listIterator());
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new UnmodifiableListIterator<>(target.listIterator(index));
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return unmodifiableList(target.subList(fromIndex, toIndex));
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        target.forEach(action);
    }

    @Override
    public Spliterator<E> spliterator() {
        return target.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public String toString() {
        return target.toString();
    }

    @Override
    public boolean equals(Object o) {
        return target.equals(o);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    private static final class UnmodifiableIterator<E> implements Iterator<E> {
        private final Iterator<E> iterator;

        private UnmodifiableIterator(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            return iterator.next();
        }
    }

    private static final class UnmodifiableListIterator<E> implements ListIterator<E> {
        private final ListIterator<E> iterator;

        private UnmodifiableListIterator(ListIterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            return iterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public E previous() {
            return iterator.previous();
        }

        @Override
        public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Modification is not supported");
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException("Modification is not supported");
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException("Modification is not supported");
        }
    }
}
