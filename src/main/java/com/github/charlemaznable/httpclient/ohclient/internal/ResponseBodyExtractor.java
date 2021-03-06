package com.github.charlemaznable.httpclient.ohclient.internal;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.ResponseBody;
import okio.BufferedSource;

import java.io.InputStream;
import java.io.Reader;
import java.util.function.Function;

import static com.github.charlemaznable.core.codec.Json.spec;
import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.codec.Json.unJsonArray;
import static com.github.charlemaznable.core.codec.Xml.unXml;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static java.util.Objects.nonNull;
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
    public static String string(ResponseBody responseBody) {
        return responseBody.string();
    }

    static Object object(ResponseBody responseBody,
                         Function<String, Object> customParser,
                         Class<?> returnType) {
        val content = string(responseBody);
        if (isBlank(content)) return null;
        if (nonNull(customParser)) return customParser.apply(content);
        if (content.startsWith("<")) return spec(unXml(content), returnType);
        if (content.startsWith("[")) return unJsonArray(content, returnType);
        if (content.startsWith("{")) return unJson(content, returnType);
        throw new IllegalArgumentException("Parse response body Error: \n" + content);
    }
}
