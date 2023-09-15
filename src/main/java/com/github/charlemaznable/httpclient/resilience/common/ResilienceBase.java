package com.github.charlemaznable.httpclient.resilience.common;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceBulkheadRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceCircuitBreakerRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRateLimiterRecover;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.netty.channel.DefaultEventLoop;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.ScheduledExecutorService;

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
    }
}
