package com.github.charlemaznable.httpclient.resilience.annotation;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceTimeLimiterRecover;

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
public @interface ResilienceTimeLimiter {

    String name() default "";

    long timeoutDurationInMillis() default 1000;

    Class<? extends ResilienceTimeLimiterRecover> fallback() default ResilienceTimeLimiterRecover.class;
}
