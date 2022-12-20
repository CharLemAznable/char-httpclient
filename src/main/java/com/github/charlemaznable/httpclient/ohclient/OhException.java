package com.github.charlemaznable.httpclient.ohclient;

import java.io.Serial;

public final class OhException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -698653648383489247L;

    public OhException(String msg) {
        super(msg);
    }
}
