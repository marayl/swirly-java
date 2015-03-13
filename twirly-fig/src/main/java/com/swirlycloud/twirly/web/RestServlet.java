/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.JsonUtil.parseStartObject;

import java.io.IOException;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public abstract class RestServlet extends HttpServlet {

    protected static Context context;

    protected final Params newParams(final HttpServletRequest req) {
        return new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                final String s = req.getParameter(name);
                final Object val;
                if (s != null) {
                    if ("depth".equals(name)) {
                        val = Integer.valueOf(s);
                    } else if ("expired".equals(name) || "internal".equals(name)) {
                        val = Boolean.valueOf(s);
                    } else {
                        val = s;
                    }
                } else {
                    val = null;
                }
                return (T) val;
            }
        };
    }

    protected final Request parseRequest(HttpServletRequest req) throws BadRequestException {
        try (JsonParser p = Json.createParser(req.getReader())) {
            parseStartObject(p);
            final Request r = new Request();
            r.parse(p);
            return r;
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    protected final void sendJsonResponse(HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    protected final void sendJsonResponse(HttpServletResponse resp, ServException e)
            throws IOException {
        e.toJson(null, resp.getWriter());
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(e.getNum());
    }

    @Override
    public final void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (context.isDevEnv()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, POST, PUT");
            resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
            resp.setHeader("Access-Control-Max-Age", "86400");
        }
    }

    public static void setContext(Context context) {
        RestServlet.context = context;
    }
}