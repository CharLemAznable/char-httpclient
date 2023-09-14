package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.configservice.ConfigFactory;
import com.github.charlemaznable.configservice.ConfigListener;
import com.github.charlemaznable.configservice.ConfigListenerRegister;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.annotation.AcceptCharset;
import com.github.charlemaznable.httpclient.annotation.ConfigureWith;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.annotation.FixedContext;
import com.github.charlemaznable.httpclient.annotation.FixedHeader;
import com.github.charlemaznable.httpclient.annotation.FixedParameter;
import com.github.charlemaznable.httpclient.annotation.FixedPathVar;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.annotation.RequestExtend;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.annotation.ResilienceBulkhead;
import com.github.charlemaznable.httpclient.annotation.ResilienceCircuitBreaker;
import com.github.charlemaznable.httpclient.annotation.ResilienceFallback;
import com.github.charlemaznable.httpclient.annotation.ResilienceRateLimiter;
import com.github.charlemaznable.httpclient.annotation.ResilienceRetry;
import com.github.charlemaznable.httpclient.annotation.ResponseParse;
import com.github.charlemaznable.httpclient.annotation.StatusFallback;
import com.github.charlemaznable.httpclient.annotation.StatusSeriesFallback;
import com.github.charlemaznable.httpclient.configurer.AcceptCharsetConfigurer;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.DefaultFallbackDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryConfigurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedContextsConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedHeadersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedParametersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedPathVarsConfigurer;
import com.github.charlemaznable.httpclient.configurer.InitializationConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingBalanceConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResilienceBulkheadConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResilienceCircuitBreakerConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResilienceFallbackConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResilienceRateLimiterConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResilienceRetryConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusFallbacksConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusSeriesFallbacksConfigurer;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.netty.channel.DefaultEventLoop;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newEnumMap;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.httpclient.common.CommonConstant.NOT_BLANK_KEY;
import static com.github.charlemaznable.httpclient.common.CommonConstant.log;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@RequiredArgsConstructor
public abstract class CommonElement<T extends CommonBase<T>> {

    @Getter
    @Accessors(fluent = true)
    final T base;
    @Getter
    @Accessors(fluent = true)
    final Factory factory;

    ConfigListener configListener;
    @Getter
    @Accessors(fluent = true)
    Configurer configurer;
    final Object configLock = new Object();

    public void initializeConfigListener(Runnable reloader) {
        configListener = (keyset, key, value) -> notNullThenRun(reloader, Runnable::run);
    }

    public void initializeConfigurer(AnnotatedElement element) {
        checkConfigurerIsRegisterThenRun(ConfigListenerRegister::removeConfigListener);
        configurer = buildConfigurer(element);
        checkConfigurerIsRegisterThenRun(ConfigListenerRegister::addConfigListener);
    }

    public void setUpBeforeInitialization(Class<?> clazz, Method method, CommonElement<T> superElement) {
        if (configurer instanceof InitializationConfigurer initializationConfigurer) {
            initializationConfigurer.setUpBeforeInitialization(clazz, method);
        } else if (nonNull(superElement)) {
            superElement.setUpBeforeInitialization(clazz, method, null);
        } else {
            InitializationConfigurer.INSTANCE.setUpBeforeInitialization(clazz, method);
        }
    }

    public void initialize(AnnotatedElement element, T superBase) {
        base.acceptCharset = nullThen(buildAcceptCharset(element), () -> superBase.acceptCharset);
        base.contentFormatter = nullThen(buildContentFormatter(element), () -> superBase.contentFormatter);
        base.httpMethod = nullThen(buildHttpMethod(element), () -> superBase.httpMethod);
        base.headers = newArrayList(superBase.headers);
        base.headers.addAll(buildFixedHeaders(element));
        base.pathVars = newArrayList(superBase.pathVars);
        base.pathVars.addAll(buildFixedPathVars(element));
        base.parameters = newArrayList(superBase.parameters);
        base.parameters.addAll(buildFixedParameters(element));
        base.contexts = newArrayList(superBase.contexts);
        base.contexts.addAll(buildFixedContexts(element));
        base.statusFallbackMapping = defaultFallback(superBase.statusFallbackMapping);
        base.statusFallbackMapping.putAll(buildStatusFallbackMapping(element));
        base.statusSeriesFallbackMapping = defaultFallback(element, superBase.statusSeriesFallbackMapping);
        base.statusSeriesFallbackMapping.putAll(buildStatusSeriesFallbackMapping(element));
        base.requestExtender = buildRequestExtender(element, superBase.requestExtender);
        base.responseParser = buildResponseParser(element, superBase.responseParser);
        base.extraUrlQueryBuilder = buildExtraUrlQueryBuilder(element, superBase.extraUrlQueryBuilder);
        base.mappingBalancer = buildMappingBalancer(element, superBase.mappingBalancer);
        base.bulkhead = buildBulkhead(element, superBase.bulkhead);
        base.bulkheadRecover = buildBulkheadRecover(element, superBase.bulkheadRecover);
        base.rateLimiter = buildRateLimiter(element, superBase.rateLimiter);
        base.rateLimiterRecover = buildRateLimiterRecover(element, superBase.rateLimiterRecover);
        base.circuitBreaker = buildCircuitBreaker(element, superBase.circuitBreaker);
        base.circuitBreakerRecover = buildCircuitBreakerRecover(element, superBase.circuitBreakerRecover);
        base.retry = buildRetry(element, superBase.retry);
        base.retryExecutor = buildRetryExecutor(element, superBase.retryExecutor);
        base.recover = buildRecover(element, superBase.recover);
    }

    public void tearDownAfterInitialization(Class<?> clazz, Method method, CommonElement<T> superElement) {
        if (configurer instanceof InitializationConfigurer initializationConfigurer) {
            initializationConfigurer.tearDownAfterInitialization(clazz, method);
        } else if (nonNull(superElement)) {
            superElement.tearDownAfterInitialization(clazz, method, null);
        } else {
            InitializationConfigurer.INSTANCE.tearDownAfterInitialization(clazz, method);
        }
    }

    public List<String> buildMappingUrls(AnnotatedElement element) {
        if (configurer instanceof MappingConfigurer mappingConfigurer)
            return newArrayList(mappingConfigurer.urls())
                    .parallelStream().map(CommonConstant::substitute).toList();
        val mapping = getMergedAnnotation(element, Mapping.class);
        return notNullThen(mapping, anno -> Arrays
                .stream(anno.value()).parallel().map(CommonConstant::substitute).toList());
    }

    /****************************************************************/

    private Configurer buildConfigurer(AnnotatedElement element) {
        val configureWith = getMergedAnnotation(element, ConfigureWith.class);
        if (isNull(configureWith)) return null;
        val configurerClass = configureWith.value();
        val buildConfigurer = FactoryContext.build(factory, configurerClass);
        if (nonNull(buildConfigurer)) return buildConfigurer;
        try {
            return ConfigFactory.configLoader(factory).getConfig(configurerClass);
        } catch (Exception e) {
            log.warn("Load Configurer by ConfigService with exception: ", e);
            return null;
        }
    }

    private void checkConfigurerIsRegisterThenRun(BiConsumer<ConfigListenerRegister, ConfigListener> runnable) {
        if (configurer instanceof ConfigListenerRegister register) {
            notNullThenRun(runnable, r -> r.accept(register, configListener));
        }
    }

    private Charset buildAcceptCharset(AnnotatedElement element) {
        if (configurer instanceof AcceptCharsetConfigurer acceptCharsetConfigurer)
            return acceptCharsetConfigurer.acceptCharset();
        val acceptCharset = getMergedAnnotation(element, AcceptCharset.class);
        return notNullThen(acceptCharset, anno -> Charset.forName(anno.value()));
    }

    private ContentFormat.ContentFormatter buildContentFormatter(AnnotatedElement element) {
        if (configurer instanceof ContentFormatConfigurer contentFormatConfigurer)
            return contentFormatConfigurer.contentFormatter();
        val contentFormat = getMergedAnnotation(element, ContentFormat.class);
        return notNullThen(contentFormat, anno -> FactoryContext.build(factory, anno.value()));
    }

    private HttpMethod buildHttpMethod(AnnotatedElement element) {
        if (configurer instanceof RequestMethodConfigurer requestMethodConfigurer)
            return requestMethodConfigurer.requestMethod();
        val requestMethod = getMergedAnnotation(element, RequestMethod.class);
        return notNullThen(requestMethod, RequestMethod::value);
    }

    private List<Pair<String, String>> buildFixedHeaders(AnnotatedElement element) {
        if (configurer instanceof FixedHeadersConfigurer fixedHeadersConfigurer)
            return newArrayList(fixedHeadersConfigurer.fixedHeaders()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedHeader.class)).stream().map(a ->
                Pair.of(a.name(), cleanupValue(a.value(), a.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    private List<Pair<String, String>> buildFixedPathVars(AnnotatedElement element) {
        if (configurer instanceof FixedPathVarsConfigurer fixedPathVarsConfigurer)
            return newArrayList(fixedPathVarsConfigurer.fixedPathVars()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedPathVar.class)).stream().map(a ->
                Pair.of(a.name(), cleanupValue(a.value(), a.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    private List<Pair<String, Object>> buildFixedParameters(AnnotatedElement element) {
        if (configurer instanceof FixedParametersConfigurer fixedParametersConfigurer)
            return newArrayList(fixedParametersConfigurer.fixedParameters()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedParameter.class)).stream().map(a ->
                Pair.of(a.name(), (Object) cleanupValue(a.value(), a.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    private List<Pair<String, Object>> buildFixedContexts(AnnotatedElement element) {
        if (configurer instanceof FixedContextsConfigurer fixedContextsConfigurer)
            return newArrayList(fixedContextsConfigurer.fixedContexts()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedContext.class)).stream().map(a ->
                Pair.of(a.name(), (Object) cleanupValue(a.value(), a.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    private static String cleanupValue(String value, boolean emptyAsCleanup) {
        return emptyThen(value, () -> emptyAsCleanup ? null : "");
    }

    private EnumMap<HttpStatus, FallbackFunction<?>>
    defaultFallback(Map<HttpStatus, FallbackFunction<?>> defaultValue) {
        return newEnumMap(HttpStatus.class, defaultValue);
    }

    private Map<HttpStatus, FallbackFunction<?>> buildStatusFallbackMapping(AnnotatedElement element) {
        if (configurer instanceof StatusFallbacksConfigurer statusFallbacksConfigurer)
            return newHashMap(statusFallbacksConfigurer.statusFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusFallback.class))
                .parallelStream().collect(toMap(StatusFallback::status,
                        anno -> buildFallbackFunction(anno.fallback())));
    }

    private EnumMap<HttpStatus.Series, FallbackFunction<?>>
    defaultFallback(AnnotatedElement element, Map<HttpStatus.Series, FallbackFunction<?>> defaultValue) {
        val disabled = configurer instanceof DefaultFallbackDisabledConfigurer disabledConfigurer
                ? disabledConfigurer.disabledDefaultFallback() : isAnnotated(element, DefaultFallbackDisabled.class);
        return newEnumMap(HttpStatus.Series.class, disabled ? null : defaultValue);
    }

    private Map<HttpStatus.Series, FallbackFunction<?>> buildStatusSeriesFallbackMapping(AnnotatedElement element) {
        if (configurer instanceof StatusSeriesFallbacksConfigurer statusSeriesFallbacksConfigurer)
            return newHashMap(statusSeriesFallbacksConfigurer.statusSeriesFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusSeriesFallback.class))
                .parallelStream().collect(toMap(StatusSeriesFallback::statusSeries,
                        anno -> buildFallbackFunction(anno.fallback())));
    }

    @SuppressWarnings("rawtypes")
    private FallbackFunction<?> buildFallbackFunction(Class<? extends FallbackFunction> fallbackClass) {
        return FactoryContext.build(factory, fallbackClass);
    }

    private RequestExtend.RequestExtender buildRequestExtender(AnnotatedElement element,
                                                               RequestExtend.RequestExtender defaultValue) {
        if (configurer instanceof RequestExtendDisabledConfigurer disabledConfigurer
                ? disabledConfigurer.disabledRequestExtend()
                : isAnnotated(element, RequestExtend.Disabled.class)) return null;
        return nullThen(buildRequestExtender(element), () -> defaultValue);
    }

    private RequestExtend.RequestExtender buildRequestExtender(AnnotatedElement element) {
        if (configurer instanceof RequestExtendConfigurer requestExtendConfigurer)
            return requestExtendConfigurer.requestExtender();
        val requestExtend = getMergedAnnotation(element, RequestExtend.class);
        return notNullThen(requestExtend, anno -> FactoryContext.build(factory, anno.value()));
    }

    private ResponseParse.ResponseParser buildResponseParser(AnnotatedElement element,
                                                             ResponseParse.ResponseParser defaultValue) {
        if (configurer instanceof ResponseParseDisabledConfigurer disabledConfigurer
                ? disabledConfigurer.disabledResponseParse()
                : isAnnotated(element, ResponseParse.Disabled.class)) return null;
        return nullThen(buildResponseParser(element), () -> defaultValue);
    }

    private ResponseParse.ResponseParser buildResponseParser(AnnotatedElement element) {
        if (configurer instanceof ResponseParseConfigurer responseParseConfigurer)
            return responseParseConfigurer.responseParser();
        val responseParse = getMergedAnnotation(element, ResponseParse.class);
        return notNullThen(responseParse, anno -> FactoryContext.build(factory, anno.value()));
    }

    private ExtraUrlQuery.ExtraUrlQueryBuilder buildExtraUrlQueryBuilder(AnnotatedElement element,
                                                                         ExtraUrlQuery.ExtraUrlQueryBuilder defaultValue) {
        if (configurer instanceof ExtraUrlQueryDisabledConfigurer disabledConfigurer
                ? disabledConfigurer.disabledExtraUrlQuery()
                : isAnnotated(element, ExtraUrlQuery.Disabled.class)) return null;
        return nullThen(buildExtraUrlQueryBuilder(element), () -> defaultValue);
    }

    private ExtraUrlQuery.ExtraUrlQueryBuilder buildExtraUrlQueryBuilder(AnnotatedElement element) {
        if (configurer instanceof ExtraUrlQueryConfigurer extraUrlQueryConfigurer)
            return extraUrlQueryConfigurer.extraUrlQueryBuilder();
        val extraUrlQuery = getMergedAnnotation(element, ExtraUrlQuery.class);
        return notNullThen(extraUrlQuery, anno -> FactoryContext.build(factory, anno.value()));
    }

    private MappingBalance.MappingBalancer buildMappingBalancer(AnnotatedElement element,
                                                                MappingBalance.MappingBalancer defaultValue) {
        return nullThen(buildMappingBalancer(element), () -> defaultValue);
    }

    private MappingBalance.MappingBalancer buildMappingBalancer(AnnotatedElement element) {
        if (configurer instanceof MappingBalanceConfigurer mappingBalanceConfigurer)
            return mappingBalanceConfigurer.mappingBalancer();
        val mappingBalance = getMergedAnnotation(element, MappingBalance.class);
        return notNullThen(mappingBalance, anno -> FactoryContext.build(factory, anno.value()));
    }

    private Bulkhead buildBulkhead(AnnotatedElement element, Bulkhead defaultValue) {
        val defaultName = "Bulkhead-" + defaultResilienceName(element);
        if (configurer instanceof ResilienceBulkheadConfigurer bulkheadConfigurer)
            return bulkheadConfigurer.bulkhead(defaultName);
        val bulkhead = getMergedAnnotation(element, ResilienceBulkhead.class);
        return checkNull(bulkhead, () -> defaultValue, anno -> Bulkhead.of(
                blankThen(anno.name(), () -> defaultName),
                BulkheadConfig.custom().maxConcurrentCalls(anno.maxConcurrentCalls())
                        .maxWaitDuration(Duration.ofMillis(anno.maxWaitDurationInMillis())).build()));
    }

    private ResilienceBulkheadRecover<?> buildBulkheadRecover(AnnotatedElement element,
                                                              ResilienceBulkheadRecover<?> defaultValue) {
        if (configurer instanceof ResilienceBulkheadConfigurer bulkheadConfigurer)
            return bulkheadConfigurer.bulkheadRecover();
        val bulkhead = getMergedAnnotation(element, ResilienceBulkhead.class);
        return checkNull(bulkhead, () -> defaultValue, anno -> FactoryContext.build(factory, anno.fallback()));
    }

    private RateLimiter buildRateLimiter(AnnotatedElement element, RateLimiter defaultValue) {
        val defaultName = "RateLimiter-" + defaultResilienceName(element);
        if (configurer instanceof ResilienceRateLimiterConfigurer rateLimiterConfigurer)
            return rateLimiterConfigurer.rateLimiter(defaultName);
        val rateLimiter = getMergedAnnotation(element, ResilienceRateLimiter.class);
        return checkNull(rateLimiter, () -> defaultValue, anno -> RateLimiter.of(
                blankThen(anno.name(), () -> defaultName),
                RateLimiterConfig.custom().limitForPeriod(anno.limitForPeriod())
                        .limitRefreshPeriod(Duration.ofNanos(anno.limitRefreshPeriodInNanos()))
                        .timeoutDuration(Duration.ofMillis(anno.timeoutDurationInMillis())).build()));
    }

    private ResilienceRateLimiterRecover<?> buildRateLimiterRecover(AnnotatedElement element,
                                                                    ResilienceRateLimiterRecover<?> defaultValue) {
        if (configurer instanceof ResilienceRateLimiterConfigurer rateLimiterConfigurer)
            return rateLimiterConfigurer.rateLimiterRecover();
        val rateLimiter = getMergedAnnotation(element, ResilienceRateLimiter.class);
        return checkNull(rateLimiter, () -> defaultValue, anno -> FactoryContext.build(factory, anno.fallback()));
    }

    private CircuitBreaker buildCircuitBreaker(AnnotatedElement element, CircuitBreaker defaultValue) {
        val defaultName = "CircuitBreaker-" + defaultResilienceName(element);
        if (configurer instanceof ResilienceCircuitBreakerConfigurer circuitBreakerConfigurer)
            return circuitBreakerConfigurer.circuitBreaker(defaultName);
        val circuitBreaker = getMergedAnnotation(element, ResilienceCircuitBreaker.class);
        return checkNull(circuitBreaker, () -> defaultValue, anno -> CircuitBreaker.of(
                blankThen(anno.name(), () -> defaultName),
                CircuitBreakerConfig.custom().slidingWindow(anno.slidingWindowSize(),
                                anno.minimumNumberOfCalls(), anno.slidingWindowType())
                        .failureRateThreshold(anno.failureRateThreshold())
                        .slowCallRateThreshold(anno.slowCallRateThreshold())
                        .slowCallDurationThreshold(Duration.ofSeconds(anno.slowCallDurationThresholdInSeconds()))
                        .automaticTransitionFromOpenToHalfOpenEnabled(anno.automaticTransitionFromOpenToHalfOpenEnabled())
                        .waitDurationInOpenState(Duration.ofSeconds(anno.waitDurationInOpenStateInSeconds()))
                        .permittedNumberOfCallsInHalfOpenState(anno.permittedNumberOfCallsInHalfOpenState())
                        .maxWaitDurationInHalfOpenState(Duration.ofSeconds(anno.maxWaitDurationInHalfOpenStateInSeconds())).build()));
    }

    private ResilienceCircuitBreakerRecover<?> buildCircuitBreakerRecover(AnnotatedElement element,
                                                                          ResilienceCircuitBreakerRecover<?> defaultValue) {
        if (configurer instanceof ResilienceCircuitBreakerConfigurer circuitBreakerConfigurer)
            return circuitBreakerConfigurer.circuitBreakerRecover();
        val circuitBreaker = getMergedAnnotation(element, ResilienceCircuitBreaker.class);
        return checkNull(circuitBreaker, () -> defaultValue, anno -> FactoryContext.build(factory, anno.fallback()));
    }

    private Retry buildRetry(AnnotatedElement element, Retry defaultValue) {
        val defaultName = "Retry-" + defaultResilienceName(element);
        if (configurer instanceof ResilienceRetryConfigurer retryConfigurer)
            return retryConfigurer.retry(defaultName);
        val retry = getMergedAnnotation(element, ResilienceRetry.class);
        return checkNull(retry, () -> defaultValue, anno -> Retry.of(
                blankThen(anno.name(), () -> defaultName),
                RetryConfig.custom().maxAttempts(anno.maxAttempts())
                        .waitDuration(Duration.ofMillis(anno.waitDurationInMillis())).build()));
    }

    private ScheduledExecutorService buildRetryExecutor(AnnotatedElement element, ScheduledExecutorService defaultValue) {
        if (configurer instanceof ResilienceRetryConfigurer retryConfigurer)
            return retryConfigurer.isolatedExecutor() ? new DefaultEventLoop() : defaultValue;
        val retry = getMergedAnnotation(element, ResilienceRetry.class);
        return checkNull(retry, () -> defaultValue, anno ->
                anno.isolatedExecutor() ? new DefaultEventLoop() : defaultValue);
    }

    private ResilienceRecover<?> buildRecover(AnnotatedElement element, ResilienceRecover<?> defaultValue) {
        if (configurer instanceof ResilienceFallbackConfigurer fallbackConfigurer)
            return fallbackConfigurer.recover();
        val fallback = getMergedAnnotation(element, ResilienceFallback.class);
        return checkNull(fallback, () -> defaultValue, anno -> FactoryContext.build(factory, anno.value()));
    }

    private String defaultResilienceName(AnnotatedElement element) {
        if (element instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        } else if (element instanceof Method method) {
            return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
        } else {
            return "Unamed#" + Integer.toHexString(element.hashCode());
        }
    }
}
