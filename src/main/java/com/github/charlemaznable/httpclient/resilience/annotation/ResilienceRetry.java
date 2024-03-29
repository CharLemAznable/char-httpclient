package com.github.charlemaznable.httpclient.resilience.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import static io.github.resilience4j.retry.RetryConfig.DEFAULT_MAX_ATTEMPTS;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResilienceRetry {

    String name() default "";

    int maxAttempts() default DEFAULT_MAX_ATTEMPTS;

    long waitDurationInMillis() default 500L;

    Class<? extends Predicate> retryOnResultPredicate() default Predicate.class;

    boolean failAfterMaxAttempts() default false;

    boolean isolatedExecutor() default false;
}
