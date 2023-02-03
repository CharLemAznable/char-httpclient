package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.google.common.base.Splitter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.charlemaznable.core.lang.Clz.isAssignable;
import static com.github.charlemaznable.core.lang.ClzPath.findClass;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.intOf;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.httpclient.configurer.ConfigurerElf.parseStringToPairList;
import static java.util.Objects.nonNull;

public interface CommonAbstractConfigurer extends
        AcceptCharsetConfigurer, ContentFormatConfigurer, RequestMethodConfigurer,
        FixedHeadersConfigurer, FixedPathVarsConfigurer, FixedParametersConfigurer, FixedContextsConfigurer,
        StatusFallbacksConfigurer, StatusSeriesFallbacksConfigurer,
        RequestExtendConfigurer, ResponseParseConfigurer, ExtraUrlQueryConfigurer, MappingBalanceConfigurer {

    String acceptCharsetName();

    @Override
    default Charset acceptCharset() {
        try {
            return Charset.forName(toStr(acceptCharsetName()));
        } catch (Exception e) {
            return null;
        }
    }

    String contentTypeName();

    @Override
    default ContentFormat.ContentFormatter contentFormatter() {
        return Optional.ofNullable(ContentFormat.ContentType.resolve(contentTypeName()))
                .map(ContentFormat.ContentType::getContentFormatter).orElse(null);
    }

    String requestMethodName();

    @Override
    default HttpMethod requestMethod() {
        try {
            return HttpMethod.valueOf(toStr(requestMethodName()));
        } catch (Exception e) {
            return null;
        }
    }

    String headers();

    @Override
    default List<Pair<String, String>> fixedHeaders() {
        return parseStringToPairList(toStr(headers()));
    }

    String pathVars();

    @Override
    default List<Pair<String, String>> fixedPathVars() {
        return parseStringToPairList(toStr(pathVars()));
    }

    String parameters();

    @Override
    default List<Pair<String, Object>> fixedParameters() {
        return parseStringToPairList(toStr(parameters()));
    }

    String contexts();

    @Override
    default List<Pair<String, Object>> fixedContexts() {
        return parseStringToPairList(toStr(contexts()));
    }

    String statusFallbacks();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping() {
        val mapping = Splitter.on("&").omitEmptyStrings().trimResults()
                .withKeyValueSeparator("=").split(toStr(statusFallbacks()));
        return mapping.entrySet().stream()
                .filter(e -> nonNull(HttpStatus.resolve(intOf(e.getKey()))))
                .filter(e -> isAssignable(findClass(e.getValue()), FallbackFunction.class))
                .collect(toMap(e -> HttpStatus.valueOf(intOf(e.getKey())),
                        e -> (Class<? extends FallbackFunction>) findClass(e.getValue())));
    }

    String statusSeriesFallbacks();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default Map<HttpStatus.Series, Class<? extends FallbackFunction>> statusSeriesFallbackMapping() {
        val mapping = Splitter.on("&").omitEmptyStrings().trimResults()
                .withKeyValueSeparator("=").split(toStr(statusSeriesFallbacks()));
        return mapping.entrySet().stream()
                .filter(e -> nonNull(HttpStatus.Series.resolve(intOf(e.getKey()))))
                .filter(e -> isAssignable(findClass(e.getValue()), FallbackFunction.class))
                .collect(toMap(e -> HttpStatus.Series.valueOf(intOf(e.getKey())),
                        e -> (Class<? extends FallbackFunction>) findClass(e.getValue())));
    }
}
