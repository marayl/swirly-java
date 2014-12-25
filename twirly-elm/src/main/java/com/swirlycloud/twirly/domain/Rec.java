/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.collection.BasicSlNode;
import com.swirlycloud.twirly.util.Identifiable;
import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Jsonifiable;

public abstract class Rec extends BasicSlNode implements Identifiable, Memorable, Jsonifiable {
    private transient Rec idNext;
    private transient Rec mnemNext;
    protected final RecType recType;
    protected final long id;
    protected final String mnem;
    protected final String display;

    public Rec(RecType recType, long id, String mnem, String display) {
        this.recType = recType;
        this.id = id;
        this.mnem = mnem;
        this.display = display;
    }

    public final void setIdNext(Rec idNext) {
        this.idNext = idNext;
    }

    public final Rec idNext() {
        return idNext;
    }

    public final void setMnemNext(Rec mnemNext) {
        this.mnemNext = mnemNext;
    }

    public final Rec mnemNext() {
        return mnemNext;
    }

    public final RecType getRecType() {
        return recType;
    }

    @Override
    public final long getId() {
        return id;
    }

    @Override
    public final String getMnem() {
        return mnem;
    }

    public final String getDisplay() {
        return display;
    }
}