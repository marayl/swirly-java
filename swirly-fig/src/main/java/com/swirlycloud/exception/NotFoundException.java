/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.exception;

public final class NotFoundException extends ServException {

    private static final long serialVersionUID = 1L;
    private static final int NUM = 404;

    public NotFoundException(String msg) {
        super(NUM, msg);
    }

    public NotFoundException(String msg, Throwable cause) {
        super(NUM, msg, cause);
    }
}