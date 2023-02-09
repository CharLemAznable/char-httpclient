package com.github.charlemaznable.httpclient.ohclient.annotation;

import okhttp3.logging.HttpLoggingInterceptor.Level;

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
public @interface ClientLoggingLevel {

    Level value() default Level.NONE;
}
