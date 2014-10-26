/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

import java.util.Collection;
import static java.util.Collections.binarySearch;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A sorted and unique set of elements. Although this collection could be considered a
 * {@link java.util.List} or {@link java.util.Set}, neither interface is currently implemented.
 * 
 * @author Mark Aylett
 * @param <T>
 *            element type.
 */
public final class SortedList<T> implements Collection<T> {

    private final LinkedList<T> list;
    private final Comparator<? super T> comp;

    public SortedList(Comparator<? super T> comp) {
        this.comp = comp;
        this.list = new LinkedList<T>();
    }

    // Collection.

    public final boolean add(T element) {
        addElement(element);
        return true;
    }

    public final boolean addAll(Collection<? extends T> c) {
        for (final T node : c) {
            add(node);
        }
        return true;
    }

    public final void clear() {
        list.clear();
    }

    @SuppressWarnings("unchecked")
    public final boolean contains(Object o) {
        return 0 <= containsElement((T) o);
    }

    @SuppressWarnings("unchecked")
    public final boolean containsAll(Collection<?> c) {
        for (final Object o : c) {
            if (containsElement((T) o) < 0) {
                return false;
            }
        }
        return true;
    }

    public final boolean isEmpty() {
        return list.isEmpty();
    }

    public final Iterator<T> iterator() {
        return list.iterator();
    }

    @SuppressWarnings("unchecked")
    public final boolean remove(Object o) {
        return 0 <= removeElement((T) o);
    }

    public final boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (final Object o : c) {
            if (remove(o)) {
                changed = true;
            }
        }
        return changed;
    }

    public final boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    public final int size() {
        return list.size();
    }

    public final Object[] toArray() {
        return list.toArray();
    }

    public final <U> U[] toArray(U[] a) {
        return list.toArray(a);
    }

    // This.

    public final int addElement(T element) {

        // Index of the search key, if it is contained in the list; otherwise,
        // (-(insertion point) - 1).

        int i = binarySearch(list, element, comp);
        if (i < 0) {
            i = -i - 1;
            list.add(i, element);
            return i;
        }

        list.set(i, element);
        return i;
    }

    public final int containsElement(T element) {
        return binarySearch(list, element, comp);
    }

    public final int removeElement(T element) {
        final int i = binarySearch(list, element, comp);
        if (0 <= i) {
            list.remove(i);
        }
        return i;
    }

    public final T removeFirst() {
        return list.removeFirst();
    }

    public final T removeLast() {
        return list.removeLast();
    }

    public final T get(int i) {
        return list.get(i);
    }

    public final T getFirst() {
        return list.getFirst();
    }

    public final T getLast() {
        return list.getLast();
    }
}