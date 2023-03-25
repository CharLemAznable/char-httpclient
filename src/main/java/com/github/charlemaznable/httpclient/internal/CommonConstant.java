package com.github.charlemaznable.httpclient.internal;

import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.google.common.net.HttpHeaders;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.function.Predicate;

import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.httpclient.common.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class CommonConstant {

    public static final String ACCEPT_CHARSET = HttpHeaders.ACCEPT_CHARSET;
    public static final Charset DEFAULT_ACCEPT_CHARSET = UTF_8;
    public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE;
    public static final ContentFormat.ContentFormatter DEFAULT_CONTENT_FORMATTER = new ContentFormat.FormContentFormatter();
    public static final HttpMethod DEFAULT_HTTP_METHOD = GET;

    public static final Predicate<Pair<String, ?>> NOT_BLANK_KEY = p -> isNotBlank(p.getKey());
}
