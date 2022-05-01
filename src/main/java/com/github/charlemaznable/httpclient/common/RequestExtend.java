package com.github.charlemaznable.httpclient.common;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestExtend {

    Class<? extends RequestExtender> value();

    interface RequestExtender {

        void extend(List<Pair<String, String>> headers,
                    List<Pair<String, String>> pathVars,
                    List<Pair<String, Object>> parameters,
                    List<Pair<String, Object>> contexts);
    }
}
