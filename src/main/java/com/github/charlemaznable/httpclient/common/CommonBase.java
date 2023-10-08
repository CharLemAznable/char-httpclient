package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.annotation.RequestExtend;
import com.github.charlemaznable.httpclient.annotation.ResponseParse;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceBase;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newEnumMap;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.httpclient.common.CommonConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonConstant.DEFAULT_HTTP_METHOD;

@NoArgsConstructor
@Getter
@Accessors(fluent = true)
public abstract class CommonBase<T extends CommonBase<T>> {

    Charset acceptCharset = DEFAULT_ACCEPT_CHARSET;
    ContentFormat.ContentFormatter contentFormatter = DEFAULT_CONTENT_FORMATTER;
    HttpMethod httpMethod = DEFAULT_HTTP_METHOD;
    List<Pair<String, String>> headers = newArrayList();
    List<Pair<String, String>> pathVars = newArrayList();
    List<Pair<String, Object>> parameters = newArrayList();
    List<Pair<String, Object>> contexts = newArrayList();

    EnumMap<HttpStatus, FallbackFunction<?>> statusFallbackMapping = newEnumMap(HttpStatus.class);
    EnumMap<HttpStatus.Series, FallbackFunction<?>> statusSeriesFallbackMapping = newEnumMap(
            HttpStatus.Series.class, of(
                    HttpStatus.Series.CLIENT_ERROR, new StatusErrorFallback(),
                    HttpStatus.Series.SERVER_ERROR, new StatusErrorFallback()));

    RequestExtend.RequestExtender requestExtender;
    ResponseParse.ResponseParser responseParser;
    ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder;
    MappingBalance.MappingBalancer mappingBalancer = new MappingBalance.RandomBalancer();

    MeterRegistry meterRegistry;

    ResilienceBase resilienceBase = new ResilienceBase();

    public CommonBase(CommonBase<?> other) {
        this.acceptCharset = other.acceptCharset;
        this.contentFormatter = other.contentFormatter;
        this.httpMethod = other.httpMethod;
        this.headers = newArrayList(other.headers);
        this.pathVars = newArrayList(other.pathVars);
        this.parameters = newArrayList(other.parameters);
        this.contexts = newArrayList(other.contexts);
        this.statusFallbackMapping = newEnumMap(
                HttpStatus.class, other.statusFallbackMapping);
        this.statusSeriesFallbackMapping = newEnumMap(
                HttpStatus.Series.class, other.statusSeriesFallbackMapping);
        this.requestExtender = other.requestExtender;
        this.responseParser = other.responseParser;
        this.extraUrlQueryBuilder = other.extraUrlQueryBuilder;
        this.mappingBalancer = other.mappingBalancer;
        this.meterRegistry = other.meterRegistry;
        this.resilienceBase = new ResilienceBase(other.resilienceBase);
    }
}
