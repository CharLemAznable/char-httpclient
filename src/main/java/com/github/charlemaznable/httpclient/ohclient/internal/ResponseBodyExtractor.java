package com.github.charlemaznable.httpclient.ohclient.internal;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import okio.BufferedSource;

import java.io.InputStream;
import java.io.Reader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ResponseBodyExtractor {

    @SneakyThrows
    static InputStream byteStream(ResponseBody responseBody) {
        return responseBody.byteStream();
    }

    static BufferedSource source(ResponseBody responseBody) {
        return responseBody.source();
    }

    @SneakyThrows
    static byte[] bytes(ResponseBody responseBody) {
        return responseBody.bytes();
    }

    static Reader charStream(ResponseBody responseBody) {
        return responseBody.charStream();
    }

    @SneakyThrows
    static String string(ResponseBody responseBody) {
        return responseBody.string();
    }
}
