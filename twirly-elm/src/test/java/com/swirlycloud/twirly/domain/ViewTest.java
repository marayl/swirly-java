/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.Test;

import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.util.Params;

public final class ViewTest {
    @Test
    public final void testToJson() throws IOException {
        final Contr contr = MockContr.newContr("EURUSD");
        final int settlDay = ymdToJd(2014, 2, 14);
        final int fixingDay = settlDay - 2;
        final int expiryDay = settlDay - 3;
        final Ladder ladder = new Ladder();

        ladder.setOfferRung(2, 12348, 30, 3);
        ladder.setOfferRung(1, 12347, 20, 2);
        ladder.setOfferRung(0, 12346, 10, 1);

        ladder.setBidRung(0, 12344, 10, 1);
        ladder.setBidRung(1, 12343, 20, 2);
        ladder.setBidRung(2, 12342, 30, 3);

        final long lastTicks = 12344;
        final long lastLots = 5;
        final long lastTime = 1414932078620L;

        final View view = new View(contr, settlDay, fixingDay, expiryDay, ladder, lastTicks,
                lastLots, lastTime);

        final StringBuilder sb = new StringBuilder();

        // Null params.
        view.toJson(null, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140312,\"expiryDate\":20140311,\"bidTicks\":[12344,12343,12342],\"bidLots\":[10,20,30],\"bidCount\":[1,2,3],\"offerTicks\":[12346,12347,12348],\"offerLots\":[10,20,30],\"offerCount\":[1,2,3],\"lastTicks\":12344,\"lastLots\":5,\"lastTime\":1414932078620}",
                sb.toString());

        // Empty params.
        sb.setLength(0);
        view.toJson(new Params() {
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140312,\"expiryDate\":20140311,\"bidTicks\":[12344,12343,12342],\"bidLots\":[10,20,30],\"bidCount\":[1,2,3],\"offerTicks\":[12346,12347,12348],\"offerLots\":[10,20,30],\"offerCount\":[1,2,3],\"lastTicks\":12344,\"lastLots\":5,\"lastTime\":1414932078620}",
                sb.toString());

        // Explicit TOB.
        sb.setLength(0);
        view.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(1) : null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140312,\"expiryDate\":20140311,\"bidTicks\":[12344],\"bidLots\":[10],\"bidCount\":[1],\"offerTicks\":[12346],\"offerLots\":[10],\"offerCount\":[1],\"lastTicks\":12344,\"lastLots\":5,\"lastTime\":1414932078620}",
                sb.toString());

        // Round-up to minimum.
        sb.setLength(0);
        view.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(-1) : null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140312,\"expiryDate\":20140311,\"bidTicks\":[12344],\"bidLots\":[10],\"bidCount\":[1],\"offerTicks\":[12346],\"offerLots\":[10],\"offerCount\":[1],\"lastTicks\":12344,\"lastLots\":5,\"lastTime\":1414932078620}",
                sb.toString());

        // Between minimum and maximum.
        sb.setLength(0);
        view.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(2) : null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140312,\"expiryDate\":20140311,\"bidTicks\":[12344,12343],\"bidLots\":[10,20],\"bidCount\":[1,2],\"offerTicks\":[12346,12347],\"offerLots\":[10,20],\"offerCount\":[1,2],\"lastTicks\":12344,\"lastLots\":5,\"lastTime\":1414932078620}",
                sb.toString());

        // Round-down to maximum.
        sb.setLength(0);
        view.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(100) : null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140312,\"expiryDate\":20140311,\"bidTicks\":[12344,12343,12342,null,null],\"bidLots\":[10,20,30,null,null],\"bidCount\":[1,2,3,null,null],\"offerTicks\":[12346,12347,12348,null,null],\"offerLots\":[10,20,30,null,null],\"offerCount\":[1,2,3,null,null],\"lastTicks\":12344,\"lastLots\":5,\"lastTime\":1414932078620}",
                sb.toString());
    }

    @Test
    public final void testParse() throws IOException {
        final Contr contr = MockContr.newContr("EURUSD");
        final int settlDay = ymdToJd(2014, 2, 14);
        final int fixingDay = settlDay - 2;
        final int expiryDay = settlDay - 3;
        final Ladder ladder = new Ladder();

        ladder.setOfferRung(2, 12348, 30, 3);
        ladder.setOfferRung(1, 12347, 20, 2);
        ladder.setOfferRung(0, 12346, 10, 1);

        ladder.setBidRung(0, 12344, 10, 1);
        ladder.setBidRung(1, 12343, 20, 2);
        ladder.setBidRung(2, 12342, 30, 3);

        final long lastTicks = 12344;
        final long lastLots = 5;
        final long lastTime = 1414932078620L;

        final View in = new View(contr, settlDay, fixingDay, expiryDay, ladder, lastTicks,
                lastLots, lastTime);

        try (JsonParser p = Json.createParser(new StringReader(in.toString()))) {
            final View out = View.parse(p);

            assertEquals(contr.getId(), out.getContrId());
            assertEquals(settlDay, out.getSettlDay());
            assertEquals(fixingDay, out.getFixingDay());
            assertEquals(expiryDay, out.getExpiryDay());

            assertEquals(0, out.getOfferTicks(3));
            assertEquals(0, out.getOfferLots(3));
            assertEquals(0, out.getOfferCount(3));

            assertEquals(12348, out.getOfferTicks(2));
            assertEquals(30, out.getOfferLots(2));
            assertEquals(3, out.getOfferCount(2));

            assertEquals(12347, out.getOfferTicks(1));
            assertEquals(20, out.getOfferLots(1));
            assertEquals(2, out.getOfferCount(1));

            assertEquals(12346, out.getOfferTicks(0));
            assertEquals(10, out.getOfferLots(0));
            assertEquals(1, out.getOfferCount(0));

            assertEquals(12344, out.getBidTicks(0));
            assertEquals(10, out.getBidLots(0));
            assertEquals(1, out.getOfferCount(0));

            assertEquals(12343, out.getBidTicks(1));
            assertEquals(20, out.getBidLots(1));
            assertEquals(2, out.getOfferCount(1));

            assertEquals(12342, out.getBidTicks(2));
            assertEquals(30, out.getBidLots(2));
            assertEquals(3, out.getOfferCount(2));

            assertEquals(0, out.getBidTicks(3));
            assertEquals(0, out.getBidLots(3));
            assertEquals(0, out.getOfferCount(3));

            assertEquals(lastTicks, out.getLastTicks());
            assertEquals(lastLots, out.getLastLots());
            assertEquals(lastTime, out.getLastTime());
        }
    }
}
