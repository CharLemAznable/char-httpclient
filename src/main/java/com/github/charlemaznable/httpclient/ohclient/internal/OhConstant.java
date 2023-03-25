package com.github.charlemaznable.httpclient.ohclient.internal;

import lombok.NoArgsConstructor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OhConstant {

    public static final long DEFAULT_CALL_TIMEOUT = 0;
    public static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
    public static final long DEFAULT_READ_TIMEOUT = 10_000;
    public static final long DEFAULT_WRITE_TIMEOUT = 10_000;
    public static final Level DEFAULT_LOGGING_LEVEL = Level.NONE;
}
