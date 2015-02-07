/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class Queue<V> {
    private V first;
    private V last;

    protected abstract void setNext(V node, V next);

    protected abstract V next(V node);

    public final void clear() {
        first = null;
        last = null;
    }

    public final void insertBack(V node) {
        if (!isEmpty()) {
            setNext(last, node);
        } else {
            first = node;
        }
        last = node;
        setNext(node, null);
    }

    public final V removeFirst() {
        if (isEmpty()) {
            return null;
        }
        final V node = first;
        first = next(first);
        if (isEmpty()) {
            last = null;
        }
        return node;
    }

    public final void join(Queue<V> rhs) {
        if (!rhs.isEmpty()) {
            if (!isEmpty()) {
                setNext(last, rhs.first);
            } else {
                first = rhs.first;
            }
            last = rhs.last;
        }
    }

    public final V getFirst() {
        return first;
    }

    public final V getLast() {
        return last;
    }

    public final boolean isEmpty() {
        return first == null;
    }
}
