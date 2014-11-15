/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

import static org.junit.Assert.*;

import org.doobry.util.BasicRbNode;
import org.doobry.util.Tree;
import org.junit.Test;

public final class TreeTest {
    private static final class Node extends BasicRbNode {
        private final long id;
        private final String name;

        Node(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public final long getId() {
            return id;
        }

        @Override
        public final String toString() {
            return name;
        }
    }

    @Test
    public final void test() {
        final Tree t = new Tree();
        final Node first = new Node(101, "first");
        final Node second = new Node(102, "second");
        final Node third = new Node(103, "third");
        assertTrue(t.isEmpty());
        assertNull(t.getFirst());
        assertNull(t.getLast());
        assertEquals(first.getColor(), 0);
        t.insert(first);
        assertNotEquals(first.getColor(), 0);
        assertFalse(t.isEmpty());
        assertSame(first, t.getFirst());
        assertSame(first, t.getLast());
        t.insert(second);
        assertSame(first, t.getFirst());
        assertSame(second, t.getLast());
        t.insert(third);
        assertSame(first, t.getFirst());
        assertSame(third, t.getLast());
        t.remove(first);
        assertEquals(first.getColor(), 0);
        assertSame(second, t.getFirst());
        assertSame(third, t.getLast());
        t.remove(second);
        t.remove(third);
        assertTrue(t.isEmpty());
        assertNull(t.getFirst());
        assertNull(t.getLast());
    }
}
