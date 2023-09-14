package com.github.charlemaznable.httpclient.common;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.CompletionStageUtils;
import io.github.resilience4j.core.SupplierUtils;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import lombok.Lombok;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Clz.isAssignable;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static java.util.Objects.requireNonNull;

interface ResilienceDecorators {

    static <T> DecorateSupplier<T> ofSupplier(Supplier<T> supplier) {
        return new DecorateSupplier<>(supplier);
    }

    static <T> DecorateCompletionStage<T> ofCompletionStage(Supplier<CompletionStage<T>> stageSupplier) {
        return new DecorateCompletionStage<>(stageSupplier);
    }

    class DecorateSupplier<T> {

        private Supplier<T> supplier;

        private DecorateSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public DecorateSupplier<T> withBulkhead(Bulkhead bulkhead,
                                                Function<BulkheadFullException, ?> bulkheadRecover) {
            notNullThenRun(bulkhead, b -> {
                supplier = Bulkhead.decorateSupplier(b, supplier);
                notNullThenRun(bulkheadRecover, br ->
                        supplier = SupplierUtils.recover(supplier,
                                fallback(BulkheadFullException.class, cast(br))));
            });
            return this;
        }

        public DecorateSupplier<T> withRateLimiter(RateLimiter rateLimiter,
                                                   Function<RequestNotPermitted, ?> rateLimiterRecover) {
            notNullThenRun(rateLimiter, r -> {
                supplier = RateLimiter.decorateSupplier(r, 1, supplier);
                notNullThenRun(rateLimiterRecover, rr ->
                        supplier = SupplierUtils.recover(supplier,
                                fallback(RequestNotPermitted.class, cast(rr))));
            });
            return this;
        }

        public DecorateSupplier<T> withCircuitBreaker(CircuitBreaker circuitBreaker,
                                                      Function<CallNotPermittedException, ?> circuitBreakerRecover) {
            notNullThenRun(circuitBreaker, c -> {
                supplier = CircuitBreaker.decorateSupplier(c, supplier);
                notNullThenRun(circuitBreakerRecover, cr ->
                        supplier = SupplierUtils.recover(supplier,
                                fallback(CallNotPermittedException.class, cast(cr))));
            });
            return this;
        }

        public DecorateSupplier<T> withRetry(Retry retry) {
            notNullThenRun(retry, r -> supplier =
                    Retry.decorateSupplier(r, supplier));
            return this;
        }

        public DecorateSupplier<T> withRecover(Function<Throwable, ?> recover) {
            notNullThenRun(recover, r -> supplier =
                    SupplierUtils.recover(supplier, cast(r)));
            return this;
        }

        public T get() {
            return supplier.get();
        }
    }

    class DecorateCompletionStage<T> {

        private Supplier<CompletionStage<T>> stageSupplier;

        public DecorateCompletionStage(Supplier<CompletionStage<T>> stageSupplier) {
            this.stageSupplier = stageSupplier;
        }

        public DecorateCompletionStage<T> withBulkhead(Bulkhead bulkhead,
                                                       Function<BulkheadFullException, ?> bulkheadRecover) {
            notNullThenRun(bulkhead, b -> {
                stageSupplier = Bulkhead.decorateCompletionStage(b, stageSupplier);
                notNullThenRun(bulkheadRecover, br ->
                        stageSupplier = CompletionStageUtils.recover(stageSupplier,
                                fallback(BulkheadFullException.class, cast(br))));
            });
            return this;
        }

        public DecorateCompletionStage<T> withRateLimiter(RateLimiter rateLimiter,
                                                          Function<RequestNotPermitted, ?> rateLimiterRecover) {
            notNullThenRun(rateLimiter, r -> {
                stageSupplier = RateLimiter.decorateCompletionStage(r, 1, stageSupplier);
                notNullThenRun(rateLimiterRecover, rr ->
                    stageSupplier = CompletionStageUtils.recover(stageSupplier,
                            fallback(RequestNotPermitted.class, cast(rr))));
            });
            return this;
        }

        public DecorateCompletionStage<T> withCircuitBreaker(CircuitBreaker circuitBreaker,
                                                             Function<CallNotPermittedException, ?> circuitBreakerRecover) {
            notNullThenRun(circuitBreaker, c -> {
                stageSupplier = CircuitBreaker.decorateCompletionStage(c, stageSupplier);
                notNullThenRun(circuitBreakerRecover, cr ->
                        stageSupplier = CompletionStageUtils.recover(stageSupplier,
                                fallback(CallNotPermittedException.class, cast(cr))));
            });
            return this;
        }

        public DecorateCompletionStage<T> withRetry(Retry retry, ScheduledExecutorService scheduler) {
            notNullThenRun(retry, r -> stageSupplier =
                    Retry.decorateCompletionStage(r, requireNonNull(scheduler), stageSupplier));
            return this;
        }

        public DecorateCompletionStage<T> withRecover(Function<Throwable, ?> recover) {
            notNullThenRun(recover, r -> stageSupplier =
                    CompletionStageUtils.recover(stageSupplier, cast(r)));
            return this;
        }

        public CompletionStage<T> get() {
            return stageSupplier.get();
        }
    }

    @SuppressWarnings("unchecked")
    private static <X, Y> Function<X, Y> cast(Function<X, ?> function) {
        return x -> (Y) function.apply(x);
    }

    private static <X extends Throwable, Y>
    Function<Throwable, Y> fallback(Class<X> exceptionType, Function<X, Y> function) {
        return throwable -> {
            if (isAssignable(throwable.getClass(), exceptionType)) {
                return function.apply(exceptionType.cast(throwable));
            } else {
                throw Lombok.sneakyThrow(throwable);
            }
        };
    }
}
