/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class QueueTest {
    @Test
    public final void test() {
        final Queue q = new Queue();
        assertTrue(q.isEmpty());
        assertNull(q.getFirst());
    }
}
