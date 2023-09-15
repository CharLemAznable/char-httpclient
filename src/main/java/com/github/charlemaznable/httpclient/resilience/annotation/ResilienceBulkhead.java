package com.github.charlemaznable.httpclient.resilience.annotation;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceBulkheadRecover;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.resilience4j.bulkhead.BulkheadConfig.DEFAULT_MAX_CONCURRENT_CALLS;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResilienceBulkhead {

    String name() default "";

    int maxConcurrentCalls() default DEFAULT_MAX_CONCURRENT_CALLS;

    long maxWaitDurationInMillis() default 0;

    Class<? extends ResilienceBulkheadRecover> fallback() default ResilienceBulkheadRecover.class;
}
