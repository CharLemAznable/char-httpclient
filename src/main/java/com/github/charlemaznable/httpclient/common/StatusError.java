package com.github.charlemaznable.httpclient.common;

import lombok.Getter;

import java.io.Serial;

@Getter
public class StatusError extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3521440029221036523L;
    private final int statusCode;

    public StatusError(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
