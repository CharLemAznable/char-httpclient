package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.httpclient.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.httpclient.common.ContentFormat.FormContentFormatter;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.google.common.net.HttpHeaders;
import lombok.NoArgsConstructor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.function.Predicate;

import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.httpclient.common.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OhConstant {

    public static final String ACCEPT_CHARSET = HttpHeaders.ACCEPT_CHARSET;
    public static final Charset DEFAULT_ACCEPT_CHARSET = UTF_8;
    public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE;
    public static final ContentFormatter DEFAULT_CONTENT_FORMATTER = new FormContentFormatter();
    public static final HttpMethod DEFAULT_HTTP_METHOD = GET;

    public static final long DEFAULT_CALL_TIMEOUT = 0;
    public static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
    public static final long DEFAULT_READ_TIMEOUT = 10_000;
    public static final long DEFAULT_WRITE_TIMEOUT = 10_000;
    public static final Level DEFAULT_LOGGING_LEVEL = Level.NONE;

    public static final int DEFAULT_MAX_REQUESTS = 64;
    public static final int DEFAULT_MAX_REQUESTS_PER_HOST = 5;

    public static final Predicate<Pair<String, ?>> NOT_BLANK_KEY = p -> isNotBlank(p.getKey());
}
