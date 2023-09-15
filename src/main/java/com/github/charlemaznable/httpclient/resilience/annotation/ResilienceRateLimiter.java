package com.github.charlemaznable.httpclient.resilience.annotation;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceRateLimiterRecover;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResilienceRateLimiter {

    String name() default "";

    int limitForPeriod() default 50;

    long limitRefreshPeriodInNanos() default 500;

    long timeoutDurationInMillis() default 5000;

    Class<? extends ResilienceRateLimiterRecover> fallback() default ResilienceRateLimiterRecover.class;
}
