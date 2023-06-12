package com.github.charlemaznable.httpclient.wfclient;

import java.io.Serial;

public final class WfException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7152444169724008525L;

    public WfException(String msg) {
        super(msg);
    }
}
