package com.github.charlemaznable.httpclient.common;

import java.io.Serial;

public final class ProviderException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1307860290366807086L;

    public ProviderException(String msg) {
        super(msg);
    }
}
