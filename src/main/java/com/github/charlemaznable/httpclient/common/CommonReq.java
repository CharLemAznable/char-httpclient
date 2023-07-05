package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;
import lombok.NoArgsConstructor;
import lombok.val;
import okhttp3.MediaType;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unchecked")
@NoArgsConstructor
public abstract class CommonReq<T extends CommonReq<T>> extends CommonBase<CommonReq<T>> {

    protected String baseUrl;
    protected String reqPath;
    protected String requestBody;

    public static Boolean permitsRequestBody(String requestMethod) {
        return okhttp3.internal.http.HttpMethod.permitsRequestBody(requestMethod);
    }

    public static String parseCharset(String contentType) {
        return checkNull(MediaType.parse(contentType), UTF_8::name, mediaType ->
                checkNull(mediaType.charset(), UTF_8::name, Charset::name));
    }

    public CommonReq(String baseUrl) {
        this();
        this.baseUrl = baseUrl;
    }

    public <U extends CommonReq<U>> CommonReq(CommonReq<U> other) {
        super(other);
        this.baseUrl = other.baseUrl;
        this.reqPath = other.reqPath;
        this.requestBody = other.requestBody;
    }

    public T req(String reqPath) {
        this.reqPath = reqPath;
        return (T) this;
    }

    public T requestBody(String requestBody) {
        this.requestBody = requestBody;
        return (T) this;
    }

    public T acceptCharset(Charset acceptCharset) {
        this.acceptCharset = acceptCharset;
        return (T) this;
    }

    public T contentFormat(ContentFormat.ContentFormatter contentFormatter) {
        this.contentFormatter = contentFormatter;
        return (T) this;
    }

    public T header(String name, String value) {
        this.headers.add(Pair.of(name, value));
        return (T) this;
    }

    public T headers(Map<String, String> headers) {
        headers.forEach(this::header);
        return (T) this;
    }

    public T parameter(String name, Object value) {
        this.parameters.add(Pair.of(name, value));
        return (T) this;
    }

    public T parameters(Map<String, Object> parameters) {
        parameters.forEach(this::parameter);
        return (T) this;
    }

    public T statusFallback(HttpStatus httpStatus, FallbackFunction<?> fallbackFunction) {
        this.statusFallbackMapping.put(httpStatus, fallbackFunction);
        return (T) this;
    }

    public T statusSeriesFallback(HttpStatus.Series httpStatusSeries, FallbackFunction<?> fallbackFunction) {
        this.statusSeriesFallbackMapping.put(httpStatusSeries, fallbackFunction);
        return (T) this;
    }

    public T extraUrlQueryBuilder(ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder) {
        this.extraUrlQueryBuilder = extraUrlQueryBuilder;
        return (T) this;
    }

    protected Map<String, Object> fetchParameterMap() {
        return this.parameters.stream().collect(toMap(Pair::getKey, Pair::getValue));
    }

    protected String concatRequestUrl(Map<String, Object> parameterMap) {
        val requestUrl = toStr(this.baseUrl).trim() + toStr(this.reqPath).trim();
        val extraUrlQuery = checkNull(this.extraUrlQueryBuilder, () -> "",
                builder -> builder.build(parameterMap, newHashMap()));
        return concatUrlQuery(requestUrl, extraUrlQuery);
    }

    public abstract static class Instance<T extends Instance<T>> extends CommonReq<Instance<T>> {

        public <U extends CommonReq<U>> Instance(CommonReq<U> other) {
            super(other);
        }

        public abstract T copy();

        @Override
        public T req(String reqPath) {
            val copy = copy();
            copy.reqPath = reqPath;
            return copy;
        }

        @Override
        public T requestBody(String requestBody) {
            val copy = copy();
            copy.requestBody = requestBody;
            return copy;
        }

        @Override
        public T acceptCharset(Charset acceptCharset) {
            val copy = copy();
            copy.acceptCharset = acceptCharset;
            return copy;
        }

        @Override
        public T contentFormat(ContentFormat.ContentFormatter contentFormatter) {
            val copy = copy();
            copy.contentFormatter = contentFormatter;
            return copy;
        }

        @Override
        public T header(String name, String value) {
            val copy = copy();
            copy.headers.add(Pair.of(name, value));
            return copy;
        }

        @Override
        public T headers(Map<String, String> headers) {
            val copy = copy();
            headers.forEach((name, value) ->
                    copy.headers.add(Pair.of(name, value)));
            return copy;
        }

        @Override
        public T parameter(String name, Object value) {
            val copy = copy();
            copy.parameters.add(Pair.of(name, value));
            return copy;
        }

        @Override
        public T parameters(Map<String, Object> parameters) {
            val copy = copy();
            parameters.forEach((name, value) ->
                    copy.parameters.add(Pair.of(name, value)));
            return copy;
        }

        @Override
        public T statusFallback(HttpStatus httpStatus, FallbackFunction<?> fallbackFunction) {
            val copy = copy();
            copy.statusFallbackMapping.put(httpStatus, fallbackFunction);
            return copy;
        }

        @Override
        public T statusSeriesFallback(HttpStatus.Series httpStatusSeries, FallbackFunction<?> fallbackFunction) {
            val copy = copy();
            copy.statusSeriesFallbackMapping.put(httpStatusSeries, fallbackFunction);
            return copy;
        }

        @Override
        public T extraUrlQueryBuilder(ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder) {
            val copy = copy();
            copy.extraUrlQueryBuilder = extraUrlQueryBuilder;
            return copy;
        }
    }
}
