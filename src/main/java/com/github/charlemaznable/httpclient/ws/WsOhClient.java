package com.github.charlemaznable.httpclient.ws;

import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.Mapping.UrlProvider;
import com.github.charlemaznable.httpclient.common.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.common.ResponseParse;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@OhClient
@RequestMethod(HttpMethod.POST)
@ContentFormat(com.github.charlemaznable.httpclient.ws.soap.SoapProcessor.class)
@ResponseParse(com.github.charlemaznable.httpclient.ws.soap.SoapProcessor.class)
@MappingMethodNameDisabled
@Mapping
public @interface WsOhClient {

    @AliasFor(annotation = Mapping.class)
    String[] value() default "";

    @AliasFor(annotation = Mapping.class)
    String[] urls() default "";

    @AliasFor(annotation = Mapping.class)
    Class<? extends UrlProvider> urlProvider() default UrlProvider.class;
}
