package com.github.charlemaznable.httpclient.resilience.annotation;

import com.github.charlemaznable.httpclient.resilience.function.ResilienceRecover;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("rawtypes")
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResilienceFallback {

    Class<? extends ResilienceRecover> value();
}
