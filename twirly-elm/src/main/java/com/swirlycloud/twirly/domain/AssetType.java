/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

public enum AssetType {
    COMMODITY(1), CORPORATE(2), CURRENCY(3), EQUITY(4), GOVERNMENT(5), INDEX(6);
    private final int id;

    private AssetType(int id) {
        this.id = id;
    }

    public static AssetType valueOf(int id) {
        AssetType val;
        switch (id) {
        case 1:
            val = AssetType.COMMODITY;
            break;
        case 2:
            val = AssetType.CORPORATE;
            break;
        case 3:
            val = AssetType.CURRENCY;
            break;
        case 4:
            val = AssetType.EQUITY;
            break;
        case 5:
            val = AssetType.GOVERNMENT;
            break;
        case 6:
            val = AssetType.INDEX;
            break;
        default:
            throw new IllegalArgumentException("invalid asset-type");
        }
        return val;
    }

    public final int intValue() {
        return this.id;
    }

}
