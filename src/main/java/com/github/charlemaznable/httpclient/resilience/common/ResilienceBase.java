package com.github.charlemaznable.httpclient.resilience.common;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceBulkheadRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceCircuitBreakerRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRateLimiterRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceTimeLimiterRecover;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetricsPublisher;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetricsPublisher;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetricsPublisher;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetricsPublisher;
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetricsPublisher;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
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
    TimeLimiter timeLimiter;
    ResilienceTimeLimiterRecover<?> timeLimiterRecover;

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

    public ResilienceBase(ResilienceBase other) {
        this.bulkhead = other.bulkhead;
        this.bulkheadRecover = other.bulkheadRecover;
        this.timeLimiter = other.timeLimiter;
        this.timeLimiterRecover = other.timeLimiterRecover;
        this.rateLimiter = other.rateLimiter;
        this.rateLimiterRecover = other.rateLimiterRecover;
        this.circuitBreaker = other.circuitBreaker;
        this.circuitBreakerRecover = other.circuitBreakerRecover;
        this.retry = other.retry;
        this.retryExecutor = other.retryExecutor;
        this.recover = other.recover;
    }

    void publishBulkheadMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, bulkhead, (m, b) ->
                new TaggedBulkheadMetricsPublisher(m).publishMetrics(b));
    }

    void removeBulkheadMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, bulkhead, (m, b) ->
                new TaggedBulkheadMetricsPublisher(m).removeMetrics(b));
    }

    void publishTimeLimiterMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, timeLimiter, (m, t) ->
                new TaggedTimeLimiterMetricsPublisher(m).publishMetrics(t));
    }

    void removeTimeLimiterMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, timeLimiter, (m, t) ->
                new TaggedTimeLimiterMetricsPublisher(m).removeMetrics(t));
    }

    void publishRateLimiterMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, rateLimiter, (m, r) ->
                new TaggedRateLimiterMetricsPublisher(m).publishMetrics(r));
    }

    void removeRateLimiterMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, rateLimiter, (m, r) ->
                new TaggedRateLimiterMetricsPublisher(m).removeMetrics(r));
    }

    void publishCircuitBreakerMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, circuitBreaker, (m, c) ->
                new TaggedCircuitBreakerMetricsPublisher(m).publishMetrics(c));
    }

    void removeCircuitBreakerMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, circuitBreaker, (m, c) ->
                new TaggedCircuitBreakerMetricsPublisher(m).removeMetrics(c));
    }

    void publishRetryMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, retry, (m, r) ->
                new TaggedRetryMetricsPublisher(m).publishMetrics(r));
    }

    void removeRetryMetrics(MeterRegistry meterRegistry) {
        notNullThenBiRun(meterRegistry, retry, (m, r) ->
                new TaggedRetryMetricsPublisher(m).removeMetrics(r));
    }

    private static <T, U> void notNullThenBiRun(T t, U u, BiConsumer<T, U> biConsumer) {
        if (isNull(t) || isNull(u)) return;
        biConsumer.accept(t, u);
    }
}
