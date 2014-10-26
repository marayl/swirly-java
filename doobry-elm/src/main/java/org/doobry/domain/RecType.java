/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public enum RecType {
    /**
     * Asset.
     */
    ASSET(1),
    /**
     * Contract.
     */
    CONTR(2),
    /**
     * Party.
     */
    PARTY(3);
    private final int value;

    private RecType(int value) {
        this.value = value;
    }

    public final int intValue() {
        return this.value;
    }
}