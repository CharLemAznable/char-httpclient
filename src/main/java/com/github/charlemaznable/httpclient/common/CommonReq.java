package com.github.charlemaznable.httpclient.common;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.DEFAULT_CONTENT_FORMATTER;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class CommonReq<T extends CommonReq<T>> {

    protected static final ContentFormat.ContentFormatter URL_QUERY_FORMATTER = new ContentFormat.FormContentFormatter();

    protected String baseUrl;

    protected String reqPath;

    protected Charset acceptCharset = DEFAULT_ACCEPT_CHARSET;
    protected ContentFormat.ContentFormatter contentFormatter = DEFAULT_CONTENT_FORMATTER;

    protected List<Pair<String, String>> headers = newArrayList();
    protected List<Pair<String, Object>> parameters = newArrayList();
    protected String requestBody;

    protected ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder;

    protected Map<HttpStatus, Class<? extends FallbackFunction>>
            statusFallbackMapping = newHashMap();
    protected Map<HttpStatus.Series, Class<? extends FallbackFunction>>
            statusSeriesFallbackMapping = of(
            HttpStatus.Series.CLIENT_ERROR, StatusErrorThrower.class,
            HttpStatus.Series.SERVER_ERROR, StatusErrorThrower.class);

    public CommonReq() {
        this((String) null);
    }

    public CommonReq(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public CommonReq(CommonReq<?> other) {
        this.baseUrl = other.baseUrl;
        this.reqPath = other.reqPath;
        this.acceptCharset = other.acceptCharset;
        this.contentFormatter = other.contentFormatter;
        this.headers = newArrayList(other.headers);
        this.parameters = newArrayList(other.parameters);
        this.requestBody = other.requestBody;
        this.extraUrlQueryBuilder = other.extraUrlQueryBuilder;
        this.statusFallbackMapping = newHashMap(other.statusFallbackMapping);
        this.statusSeriesFallbackMapping = newHashMap(other.statusSeriesFallbackMapping);
    }

    public T req(String reqPath) {
        this.reqPath = reqPath;
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

    public T requestBody(String requestBody) {
        this.requestBody = requestBody;
        return (T) this;
    }

    public T extraUrlQueryBuilder(ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder) {
        this.extraUrlQueryBuilder = extraUrlQueryBuilder;
        return (T) this;
    }

    public T statusFallback(HttpStatus httpStatus,
                            Class<? extends FallbackFunction> errorClass) {
        this.statusFallbackMapping.put(httpStatus, errorClass);
        return (T) this;
    }

    public T statusSeriesFallback(HttpStatus.Series httpStatusSeries,
                                  Class<? extends FallbackFunction> errorClass) {
        this.statusSeriesFallbackMapping.put(httpStatusSeries, errorClass);
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

    protected static final class Instance extends CommonReq<Instance> {

        public Instance(CommonReq<?> other) {
            super(other);
        }

        public Instance copy() {
            return new Instance(this);
        }
    }
}
