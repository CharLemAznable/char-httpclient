package com.github.charlemaznable.httpclient.resilience.common;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceBulkhead;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceCircuitBreaker;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceCircuitBreakerState;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceFallback;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceRateLimiter;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceRetry;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceTimeLimiter;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceBulkheadConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceCircuitBreakerConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceFallbackConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceRateLimiterConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceRetryConfigurer;
import com.github.charlemaznable.httpclient.resilience.configurer.ResilienceTimeLimiterConfigurer;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceBulkheadRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceCircuitBreakerRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRateLimiterRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceTimeLimiterRecover;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.channel.DefaultEventLoop;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.httpclient.resilience.common.ResilienceDefaults.checkResultPredicate;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;

@RequiredArgsConstructor
public final class ResilienceElement {

    final ResilienceBase base;
    final Factory factory;
    final Configurer configurer;

    public void initialize(AnnotatedElement element, ResilienceBase superBase) {
        base.bulkhead = buildBulkhead(element, superBase.bulkhead);
        base.bulkheadRecover = buildBulkheadRecover(element, superBase.bulkheadRecover);

        base.timeLimiter = buildTimeLimiter(element, superBase.timeLimiter);
        base.timeLimiterRecover = buildTimeLimiterRecover(element, superBase.timeLimiterRecover);

        base.rateLimiter = buildRateLimiter(element, superBase.rateLimiter);
        base.rateLimiterRecover = buildRateLimiterRecover(element, superBase.rateLimiterRecover);

        base.circuitBreaker = buildCircuitBreaker(element, superBase.circuitBreaker);
        base.circuitBreakerRecover = buildCircuitBreakerRecover(element, superBase.circuitBreakerRecover);

        base.retry = buildRetry(element, superBase.retry);
        base.retryExecutor = buildRetryExecutor(element, superBase.retryExecutor);
        base.recover = buildRecover(element, superBase.recover);

        base.meterRegistry = superBase.meterRegistry;
    }

    public void setMeterRegistry(MeterRegistry registry) {
        base.meterRegistry = registry;
    }

    public void removeMetrics() {
        base.removeBulkheadMetrics();
        base.removeTimeLimiterMetrics();
        base.removeRateLimiterMetrics();
        base.removeCircuitBreakerMetrics();
        base.removeRetryMetrics();
    }

    public void publishMetrics() {
        base.publishBulkheadMetrics();
        base.publishTimeLimiterMetrics();
        base.publishRateLimiterMetrics();
        base.publishCircuitBreakerMetrics();
        base.publishRetryMetrics();
    }

    private Bulkhead buildBulkhead(AnnotatedElement element, Bulkhead defaultValue) {
        val defaultName = "Bulkhead-" + defaultResilienceName(element);
        if (configurer instanceof ResilienceBulkheadConfigurer bulkheadConfigurer)
            return bulkheadConfigurer.bulkhead(defaultName);
        val bulkhead = getMergedAnnotation(element, ResilienceBulkhead.class);
        return checkNull(bulkhead, () -> defaultValue, anno -> Bulkhead.of(
                blankThen(anno.name(), () -> defaultName), BulkheadConfig.custom()
                        .maxConcurrentCalls(anno.maxConcurrentCalls())
                        .maxWaitDuration(Duration.ofMillis(anno.maxWaitDurationInMillis())).build()));
    }

    private ResilienceBulkheadRecover<?> buildBulkheadRecover(AnnotatedElement element,
                                                              ResilienceBulkheadRecover<?> defaultValue) {
        if (configurer instanceof ResilienceBulkheadConfigurer bulkheadConfigurer)
            return bulkheadConfigurer.bulkheadRecover();
        val bulkhead = getMergedAnnotation(element, ResilienceBulkhead.class);
        return checkNull(bulkhead, () -> defaultValue, anno -> FactoryContext.build(factory, anno.fallback()));
    }

    private TimeLimiter buildTimeLimiter(AnnotatedElement element, TimeLimiter defaultValue) {
        val defaultName = "TimeLimiter-" + defaultResilienceName(element);
        if (configurer instanceof ResilienceTimeLimiterConfigurer timeLimiterConfigurer)
            return timeLimiterConfigurer.timeLimiter(defaultName);
        val timeLimiter = getMergedAnnotation(element, ResilienceTimeLimiter.class);
        return checkNull(timeLimiter, () -> defaultValue, anno -> TimeLimiter.of(
                blankThen(anno.name(), () -> defaultName), TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofMillis(anno.timeoutDurationInMillis())).build()));
    }

    private ResilienceTimeLimiterRecover<?> buildTimeLimiterRecover(AnnotatedElement element,
                                                                    ResilienceTimeLimiterRecover<?> defaultValue) {
        if (configurer instanceof ResilienceTimeLimiterConfigurer timeLimiterConfigurer)
            return timeLimiterConfigurer.timeLimiterRecover();
        val timeLimiter = getMergedAnnotation(element, ResilienceTimeLimiter.class);
        return checkNull(timeLimiter, () -> defaultValue, anno -> FactoryContext.build(factory, anno.fallback()));
    }

    private RateLimiter buildRateLimiter(AnnotatedElement element, RateLimiter defaultValue) {
        val defaultName = "RateLimiter-" + defaultResilienceName(element);
        if (configurer instanceof ResilienceRateLimiterConfigurer rateLimiterConfigurer)
            return rateLimiterConfigurer.rateLimiter(defaultName);
        val rateLimiter = getMergedAnnotation(element, ResilienceRateLimiter.class);
        return checkNull(rateLimiter, () -> defaultValue, anno -> RateLimiter.of(
                blankThen(anno.name(), () -> defaultName), RateLimiterConfig.custom()
                        .limitForPeriod(anno.limitForPeriod())
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
        return checkNull(circuitBreaker, () -> defaultValue, anno -> {
            val state = nullThen(anno.state(), () -> ResilienceCircuitBreakerState.ENABLED);
            return state.transitionState(CircuitBreaker.of(
                    blankThen(anno.name(), () -> defaultName), CircuitBreakerConfig.custom()
                            .slidingWindow(anno.slidingWindowSize(), anno.minimumNumberOfCalls(), anno.slidingWindowType())
                            .failureRateThreshold(anno.failureRateThreshold())
                            .slowCallRateThreshold(anno.slowCallRateThreshold())
                            .slowCallDurationThreshold(Duration.ofSeconds(anno.slowCallDurationThresholdInSeconds()))
                            .recordResult(checkResultPredicate(FactoryContext.build(factory, anno.recordResultPredicate())))
                            .automaticTransitionFromOpenToHalfOpenEnabled(anno.automaticTransitionFromOpenToHalfOpenEnabled())
                            .waitDurationInOpenState(Duration.ofSeconds(anno.waitDurationInOpenStateInSeconds()))
                            .permittedNumberOfCallsInHalfOpenState(anno.permittedNumberOfCallsInHalfOpenState())
                            .maxWaitDurationInHalfOpenState(Duration.ofSeconds(anno.maxWaitDurationInHalfOpenStateInSeconds())).build()));
        });
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
                blankThen(anno.name(), () -> defaultName), RetryConfig.custom()
                        .maxAttempts(anno.maxAttempts())
                        .waitDuration(Duration.ofMillis(anno.waitDurationInMillis()))
                        .retryOnResult(checkResultPredicate(FactoryContext.build(factory, anno.retryOnResultPredicate())))
                        .failAfterMaxAttempts(anno.failAfterMaxAttempts()).build()));
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
