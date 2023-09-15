package com.github.charlemaznable.httpclient.resilience.common;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceBulkheadRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceCircuitBreakerRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRateLimiterRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetricsPublisher;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetricsPublisher;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetricsPublisher;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetricsPublisher;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.channel.DefaultEventLoop;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;

@NoArgsConstructor
@Getter
@Accessors(fluent = true)
public final class ResilienceBase {

    @Setter
    Bulkhead bulkhead;
    ResilienceBulkheadRecover<?> bulkheadRecover;

    @Setter
    RateLimiter rateLimiter;
    ResilienceRateLimiterRecover<?> rateLimiterRecover;

    @Setter
    CircuitBreaker circuitBreaker;
    ResilienceCircuitBreakerRecover<?> circuitBreakerRecover;

    @Setter
    Retry retry;
    ScheduledExecutorService retryExecutor = new DefaultEventLoop();

    ResilienceRecover<?> recover;

    MeterRegistry meterRegistry;

    public ResilienceBase(ResilienceBase other) {
        this.bulkhead = other.bulkhead;
        this.bulkheadRecover = other.bulkheadRecover;
        this.rateLimiter = other.rateLimiter;
        this.rateLimiterRecover = other.rateLimiterRecover;
        this.circuitBreaker = other.circuitBreaker;
        this.circuitBreakerRecover = other.circuitBreakerRecover;
        this.retry = other.retry;
        this.retryExecutor = other.retryExecutor;
        this.recover = other.recover;
        this.meterRegistry = other.meterRegistry;
    }

    void publishBulkheadMetrics() {
        notNullThenBiRun(meterRegistry, bulkhead, (m, b) ->
                new TaggedBulkheadMetricsPublisher(m).publishMetrics(b));
    }

    void removeBulkheadMetrics() {
        notNullThenBiRun(meterRegistry, bulkhead, (m, b) ->
                new TaggedBulkheadMetricsPublisher(m).removeMetrics(b));
    }

    void publishRateLimiterMetrics() {
        notNullThenBiRun(meterRegistry, rateLimiter, (m, r) ->
                new TaggedRateLimiterMetricsPublisher(m).publishMetrics(r));
    }

    void removeRateLimiterMetrics() {
        notNullThenBiRun(meterRegistry, rateLimiter, (m, r) ->
                new TaggedRateLimiterMetricsPublisher(m).removeMetrics(r));
    }

    void publishCircuitBreakerMetrics() {
        notNullThenBiRun(meterRegistry, circuitBreaker, (m, c) ->
                new TaggedCircuitBreakerMetricsPublisher(m).publishMetrics(c));
    }

    void removeCircuitBreakerMetrics() {
        notNullThenBiRun(meterRegistry, circuitBreaker, (m, c) ->
                new TaggedCircuitBreakerMetricsPublisher(m).removeMetrics(c));
    }

    void publishRetryMetrics() {
        notNullThenBiRun(meterRegistry, retry, (m, r) ->
                new TaggedRetryMetricsPublisher(m).publishMetrics(r));
    }

    void removeRetryMetrics() {
        notNullThenBiRun(meterRegistry, retry, (m, r) ->
                new TaggedRetryMetricsPublisher(m).removeMetrics(r));
    }

    private static <T, U> void notNullThenBiRun(T t, U u, BiConsumer<T, U> biConsumer) {
        if (isNull(t) || isNull(u)) return;
        biConsumer.accept(t, u);
    }
}
