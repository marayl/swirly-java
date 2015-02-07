/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.math.MathUtil.nextPow2;

import java.io.PrintStream;

public abstract class HashTable<V> {
    private static final Object[] EMPTY = {};
    private static final int MIN_BUCKETS = 1 << 4;

    protected Object[] buckets;
    protected int size;
    private int threshold;

    protected abstract void setNext(V node, V next);

    protected abstract V next(V node);

    protected abstract int hashKey(V node);

    protected abstract boolean equalKey(V lhs, V rhs);

    protected static int indexFor(int h, int length) {
        // Doug Lea's supplemental secondaryHash function.
        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7) ^ (h >>> 4);
        // Assumes that length is a power of two.
        return h & (length - 1);
    }

    @SuppressWarnings("unchecked")
    protected final V getBucket(int i) {
        return (V) buckets[i];
    }

    /**
     * The threshold at which the hash-table will grow.
     * 
     * @param capacity
     *            The number of buckets.
     * @return the threshold.
     */
    private static int thresholdFor(int capacity) {
        // Threshold is 2/3 capacity.
        return (capacity << 1) / 3;
    }

    @SuppressWarnings("unchecked")
    private final void grow(int capacity) {
        final Object[] newBuckets = new Object[capacity];
        for (int i = 0; i < buckets.length; ++i) {
            while (buckets[i] != null) {
                // Pop.
                final V node = getBucket(i);
                buckets[i] = next(node);
                // Push.
                final int j = indexFor(hashKey(node), newBuckets.length);
                setNext(node, (V) newBuckets[j]);
                newBuckets[j] = node;
            }
        }
        buckets = newBuckets;
        threshold = thresholdFor(capacity);
    }

    public HashTable(int capacity) {
        // Postpone allocation until first insert.
        this.buckets = EMPTY;
        this.size = 0;
        // N.B. the threshold is used to store the desired capacity on construction. It is not used
        // for its intended purpose until grow() is called.
        if (capacity < MIN_BUCKETS) {
            threshold = MIN_BUCKETS;
        } else {
            threshold = nextPow2(capacity);
        }
    }

    /**
     * Insert new element into hash table or optionally replace existing.
     * 
     * @param node
     *            The node to be inserted.
     * @param replace
     *            Flag indicating whether existing node should be replaced.
     * @return existing (and possibly replaced) node with matching key or null if no replacement
     *         took place.
     */
    public final V insert(V node, boolean replace) {
        if (buckets.length == 0) {
            // First item.
            grow(threshold);
        }
        int i = indexFor(hashKey(node), buckets.length);
        V it = getBucket(i);
        if (it != null) {
            // Check if the first element in the bucket has an equivalent key.
            if (equalKey(it, node)) {
                if (replace) {
                    setNext(node, next(it));
                    buckets[i] = node;
                }
                return it;
            }
            // Check if a subsequent element in the bucket has an equivalent key.
            for (V next; (next = next(it)) != null; it = next) {
                if (equalKey(next, node)) {
                    if (replace) {
                        // Replace.
                        setNext(node, next(next));
                        setNext(it, node);
                    }
                    return next;
                }
            }
        }
        // This is a new element.
        if (size >= threshold) {
            grow(buckets.length << 1);
            i = indexFor(hashKey(node), buckets.length);
            it = getBucket(i);
        }
        setNext(node, it);
        buckets[i] = node;
        ++size;
        return null;
    }

    public final V insert(V node) {
        // Replace existing by default.
        return insert(node, true);
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public final int size() {
        return size;
    }

    // Package-scope debugging.

    final void print(PrintStream s) {
        for (int i = 0; i < buckets.length; ++i) {
            s.print('|');
            for (V it = getBucket(i); it != null; it = next(it)) {
                s.print('*');
            }
            s.println();
        }
    }
}
