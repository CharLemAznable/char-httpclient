package com.github.charlemaznable.httpclient.resilience.annotation;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public enum ResilienceCircuitBreakerState {

    ENABLED {
        @Override
        public CircuitBreaker transitionState(CircuitBreaker circuitBreaker) {
            return circuitBreaker;
        }
    },
    DISABLED {
        @Override
        public CircuitBreaker transitionState(CircuitBreaker circuitBreaker) {
            circuitBreaker.transitionToDisabledState();
            return circuitBreaker;
        }
    },
    METRICS_ONLY {
        @Override
        public CircuitBreaker transitionState(CircuitBreaker circuitBreaker) {
            circuitBreaker.transitionToMetricsOnlyState();
            return circuitBreaker;
        }
    },
    FORCED_OPEN {
        @Override
        public CircuitBreaker transitionState(CircuitBreaker circuitBreaker) {
            circuitBreaker.transitionToForcedOpenState();
            return circuitBreaker;
        }
    };

    public abstract CircuitBreaker transitionState(CircuitBreaker circuitBreaker);
}
