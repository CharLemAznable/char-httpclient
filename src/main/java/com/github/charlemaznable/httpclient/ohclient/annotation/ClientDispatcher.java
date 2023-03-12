package com.github.charlemaznable.httpclient.ohclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_MAX_REQUESTS;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_MAX_REQUESTS_PER_HOST;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientDispatcher {

    int maxRequests() default DEFAULT_MAX_REQUESTS;

    int maxRequestsPerHost() default DEFAULT_MAX_REQUESTS_PER_HOST;
}
