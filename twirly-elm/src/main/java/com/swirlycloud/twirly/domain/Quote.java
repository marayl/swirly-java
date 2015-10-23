/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.util.NullUtil.nullIfEmpty;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class Quote extends BasicRbNode implements Jsonifiable, Request {

    private static final long serialVersionUID = 1L;

    private final long id;
    /**
     * The executing trader.
     */
    private final String trader;
    private final String market;
    private final String contr;
    private final int settlDay;
    /**
     * Ref is optional.
     */
    private final @Nullable String ref;
    private final long bidTicks;
    private final long bidLots;
    private final long offerTicks;
    private final long offerLots;
    long created;
    long expiry;

    Quote(long id, String trader, String market, String contr, int settlDay, @Nullable String ref,
            long bidTicks, long bidLots, long offerTicks, long offerLots, long created,
            long expiry) {
        this.id = id;
        this.trader = trader;
        this.market = market;
        this.contr = contr;
        this.settlDay = settlDay;
        this.ref = nullIfEmpty(ref);
        this.bidTicks = bidTicks;
        this.bidLots = bidLots;
        this.offerTicks = offerTicks;
        this.offerLots = offerLots;
        this.created = created;
        this.expiry = expiry;
    }

    public static Quote parse(JsonParser p) throws IOException {
        long id = 0;
        String trader = null;
        String market = null;
        String contr = null;
        int settlDay = 0;
        String ref = null;
        long bidTicks = 0;
        long bidLots = 0;
        long offerTicks = 0;
        long offerLots = 0;
        long created = 0;
        long expiry = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                if (trader == null) {
                    throw new IOException("trader is null");
                }
                if (market == null) {
                    throw new IOException("market is null");
                }
                if (contr == null) {
                    throw new IOException("contr is null");
                }
                return new Quote(id, trader, market, contr, settlDay, ref, bidTicks, bidLots,
                        offerTicks, offerLots, created, expiry);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NULL:
                if ("settlDate".equals(name)) {
                    settlDay = 0;
                } else if ("ref".equals(name)) {
                    ref = "";
                } else {
                    throw new IOException(String.format("unexpected null field '%s'", name));
                }
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else if ("settlDate".equals(name)) {
                    settlDay = JulianDay.maybeIsoToJd(p.getInt());
                } else if ("bidTicks".equals(name)) {
                    bidTicks = p.getLong();
                } else if ("bidLots".equals(name)) {
                    bidLots = p.getLong();
                } else if ("offerTicks".equals(name)) {
                    offerTicks = p.getLong();
                } else if ("offerLots".equals(name)) {
                    offerLots = p.getLong();
                } else if ("created".equals(name)) {
                    created = p.getLong();
                } else if ("expiry".equals(name)) {
                    expiry = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("trader".equals(name)) {
                    trader = p.getString();
                } else if ("market".equals(name)) {
                    market = p.getString();
                } else if ("contr".equals(name)) {
                    contr = p.getString();
                } else if ("ref".equals(name)) {
                    ref = p.getString();
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + market.hashCode();
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public final boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Quote other = (Quote) obj;
        if (!market.equals(other.market)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
        out.append("{\"id\":").append(String.valueOf(id));
        out.append(",\"trader\":\"").append(trader);
        out.append("\",\"market\":\"").append(market);
        out.append("\",\"contr\":\"").append(contr);
        out.append("\",\"settlDate\":");
        if (settlDay != 0) {
            out.append(String.valueOf(jdToIso(settlDay)));
        } else {
            out.append("null");
        }
        out.append(",\"ref\":");
        if (ref != null) {
            out.append('"').append(ref).append('"');
        } else {
            out.append("null");
        }
        out.append("\",\"bidTicks\":").append(String.valueOf(bidTicks));
        out.append(",\"bidLots\":").append(String.valueOf(bidLots));
        out.append("\",\"offerTicks\":").append(String.valueOf(offerTicks));
        out.append(",\"offerLots\":").append(String.valueOf(offerLots));
        out.append(",\"created\":").append(String.valueOf(created));
        out.append(",\"expiry\":").append(String.valueOf(expiry));
        out.append("}");
    }

    @Override
    public final long getId() {
        return id;
    }

    @Override
    public final String getTrader() {
        return trader;
    }

    @Override
    public final String getMarket() {
        return market;
    }

    @Override
    public final String getContr() {
        return contr;
    }

    @Override
    public final int getSettlDay() {
        return settlDay;
    }

    @Override
    public final boolean isSettlDaySet() {
        return settlDay != 0;
    }

    @Override
    public final @Nullable String getRef() {
        return ref;
    }

    public final long getBidTicks() {
        return bidTicks;
    }

    public final long getBidLots() {
        return bidLots;
    }

    public final long getOfferTicks() {
        return offerTicks;
    }

    public final long getOfferLots() {
        return offerLots;
    }

    @Override
    public final long getCreated() {
        return created;
    }

    public final long getExpiry() {
        return expiry;
    }
}
