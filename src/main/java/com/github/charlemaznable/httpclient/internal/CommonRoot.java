package com.github.charlemaznable.httpclient.internal;

import com.github.charlemaznable.configservice.ConfigFactory;
import com.github.charlemaznable.configservice.ConfigListenerRegister;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.AcceptCharset;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.FixedContext;
import com.github.charlemaznable.httpclient.common.FixedHeader;
import com.github.charlemaznable.httpclient.common.FixedParameter;
import com.github.charlemaznable.httpclient.common.FixedPathVar;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.github.charlemaznable.httpclient.common.RequestExtend;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.common.ResponseParse;
import com.github.charlemaznable.httpclient.common.StatusFallback;
import com.github.charlemaznable.httpclient.common.StatusSeriesFallback;
import com.github.charlemaznable.httpclient.configurer.AcceptCharsetConfigurer;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedContextsConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedHeadersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedParametersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedPathVarsConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingBalanceConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusFallbacksConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusSeriesFallbacksConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.AnnotatedElement;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.NOT_BLANK_KEY;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.log;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedRepeatableAnnotations;

@SuppressWarnings("rawtypes")
@Getter
@Accessors(fluent = true)
public class CommonRoot {

    protected Charset acceptCharset;
    protected ContentFormat.ContentFormatter contentFormatter;
    protected HttpMethod httpMethod;
    protected List<Pair<String, String>> headers;
    protected List<Pair<String, String>> pathVars;
    protected List<Pair<String, Object>> parameters;
    protected List<Pair<String, Object>> contexts;

    protected Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping;
    protected Map<HttpStatus.Series, Class<? extends FallbackFunction>> statusSeriesFallbackMapping;

    protected RequestExtend.RequestExtender requestExtender;
    protected ResponseParse.ResponseParser responseParser;

    protected ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder;

    protected MappingBalance.MappingBalancer mappingBalancer;

    protected static Configurer checkConfigurer(AnnotatedElement element, Factory factory) {
        val configureWith = getMergedAnnotation(element, ConfigureWith.class);
        if (isNull(configureWith)) return null;
        val configurerClass = configureWith.value();
        val configurer = FactoryContext.build(factory, configurerClass);
        if (nonNull(configurer)) return configurer;
        try {
            return ConfigFactory.configLoader(factory).getConfig(configurerClass);
        } catch (Exception e) {
            log.warn("Load Configurer by ConfigService with exception: ", e);
            return null;
        }
    }

    protected static void checkConfigurerIsRegisterThenRun(Configurer configurer,
                                                           Consumer<ConfigListenerRegister> consumer) {
        if (configurer instanceof ConfigListenerRegister register) {
            notNullThenRun(consumer, c -> c.accept(register));
        }
    }

    protected static List<String> checkMappingUrls(Configurer configurer, AnnotatedElement element,
                                                   Function<String, String> substitutor) {
        if (configurer instanceof MappingConfigurer mappingConfigurer)
            return newArrayList(mappingConfigurer.urls())
                    .stream().map(substitutor).toList();
        val mapping = getMergedAnnotation(element, Mapping.class);
        return notNullThen(mapping, anno -> Arrays
                .stream(anno.value()).map(substitutor).toList());
    }

    protected static Charset checkAcceptCharset(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof AcceptCharsetConfigurer acceptCharsetConfigurer)
            return acceptCharsetConfigurer.acceptCharset();
        val acceptCharset = getMergedAnnotation(element, AcceptCharset.class);
        return notNullThen(acceptCharset, anno -> Charset.forName(anno.value()));
    }

    protected static ContentFormat.ContentFormatter checkContentFormatter(Configurer configurer,
                                                                          AnnotatedElement element, Factory factory) {
        if (configurer instanceof ContentFormatConfigurer contentFormatConfigurer)
            return contentFormatConfigurer.contentFormatter();
        val contentFormat = getMergedAnnotation(element, ContentFormat.class);
        return notNullThen(contentFormat, anno -> FactoryContext.build(factory, anno.value()));
    }

    protected static HttpMethod checkHttpMethod(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof RequestMethodConfigurer requestMethodConfigurer)
            return requestMethodConfigurer.requestMethod();
        val requestMethod = getMergedAnnotation(element, RequestMethod.class);
        return notNullThen(requestMethod, RequestMethod::value);
    }

    protected static List<Pair<String, String>> checkFixedHeaders(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedHeadersConfigurer fixedHeadersConfigurer)
            return newArrayList(fixedHeadersConfigurer.fixedHeaders()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedHeader.class))
                .stream().map(anno -> Pair.of(anno.name(), cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    protected static List<Pair<String, String>> checkFixedPathVars(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedPathVarsConfigurer fixedPathVarsConfigurer)
            return newArrayList(fixedPathVarsConfigurer.fixedPathVars()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedPathVar.class))
                .stream().map(anno -> Pair.of(anno.name(), cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    protected static List<Pair<String, Object>> checkFixedParameters(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedParametersConfigurer fixedParametersConfigurer)
            return newArrayList(fixedParametersConfigurer.fixedParameters()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedParameter.class))
                .stream().map(anno -> Pair.of(anno.name(), (Object) cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    protected static List<Pair<String, Object>> checkFixedContexts(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedContextsConfigurer fixedContextsConfigurer)
            return newArrayList(fixedContextsConfigurer.fixedContexts()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedContext.class))
                .stream().map(anno -> Pair.of(anno.name(), (Object) cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    private static String cleanupValue(String value, boolean emptyAsCleanup) {
        return emptyThen(value, () -> emptyAsCleanup ? null : "");
    }

    protected static Map<HttpStatus, Class<? extends FallbackFunction>>
    checkStatusFallbackMapping(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof StatusFallbacksConfigurer statusFallbacksConfigurer)
            return newHashMap(statusFallbacksConfigurer.statusFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusFallback.class))
                .stream().collect(toMap(StatusFallback::status, StatusFallback::fallback));
    }

    protected static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
    checkStatusSeriesFallbackMapping(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof StatusSeriesFallbacksConfigurer statusSeriesFallbacksConfigurer)
            return newHashMap(statusSeriesFallbacksConfigurer.statusSeriesFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusSeriesFallback.class))
                .stream().collect(toMap(StatusSeriesFallback::statusSeries, StatusSeriesFallback::fallback));
    }

    protected static RequestExtend.RequestExtender checkRequestExtender(Configurer configurer,
                                                                        AnnotatedElement element, Factory factory) {
        if (configurer instanceof RequestExtendConfigurer requestExtendConfigurer)
            return requestExtendConfigurer.requestExtender();
        val requestExtend = getMergedAnnotation(element, RequestExtend.class);
        return notNullThen(requestExtend, anno -> FactoryContext.build(factory, anno.value()));
    }

    protected static ResponseParse.ResponseParser checkResponseParser(Configurer configurer,
                                                                      AnnotatedElement element, Factory factory) {
        if (configurer instanceof ResponseParseConfigurer responseParseConfigurer)
            return responseParseConfigurer.responseParser();
        val responseParse = getMergedAnnotation(element, ResponseParse.class);
        return notNullThen(responseParse, anno -> FactoryContext.build(factory, anno.value()));
    }

    protected static ExtraUrlQuery.ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(Configurer configurer,
                                                                                  AnnotatedElement element, Factory factory) {
        if (configurer instanceof ExtraUrlQueryConfigurer extraUrlQueryConfigurer)
            return extraUrlQueryConfigurer.extraUrlQueryBuilder();
        val extraUrlQuery = getMergedAnnotation(element, ExtraUrlQuery.class);
        return notNullThen(extraUrlQuery, anno -> FactoryContext.build(factory, anno.value()));
    }

    protected static MappingBalance.MappingBalancer checkMappingBalancer(Configurer configurer,
                                                                         AnnotatedElement element, Factory factory) {
        if (configurer instanceof MappingBalanceConfigurer mappingBalanceConfigurer)
            return mappingBalanceConfigurer.mappingBalancer();
        val mappingBalance = getMergedAnnotation(element, MappingBalance.class);
        return notNullThen(mappingBalance, anno -> FactoryContext.build(factory, anno.value()));
    }
}
