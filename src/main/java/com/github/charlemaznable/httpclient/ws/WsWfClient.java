package com.github.charlemaznable.httpclient.ws;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.annotation.RequestMethod;
import com.github.charlemaznable.httpclient.annotation.ResponseParse;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.wfclient.WfClient;
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
@WfClient
@RequestMethod(HttpMethod.POST)
@ContentFormat(com.github.charlemaznable.httpclient.ws.soap.SoapProcessor.class)
@ResponseParse(com.github.charlemaznable.httpclient.ws.soap.SoapProcessor.class)
@MappingMethodNameDisabled
@Mapping
public @interface WsWfClient {

    @AliasFor(annotation = Mapping.class)
    String[] value() default "";

    @AliasFor(annotation = Mapping.class)
    String[] urls() default "";
}
