package com.github.charlemaznable.httpclient.vxclient;

import java.io.Serial;

public final class VxException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4064920583673899451L;

    public VxException(Throwable cause) {
        super(cause);
    }
}
