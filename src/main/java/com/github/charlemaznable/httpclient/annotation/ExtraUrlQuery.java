package com.github.charlemaznable.httpclient.annotation;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtraUrlQuery {

    Class<? extends ExtraUrlQueryBuilder> value();

    interface ExtraUrlQueryBuilder {

        String build(@Nonnull final Map<String, Object> parameterMap,
                     @Nonnull final Map<String, Object> contextMap);
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Disabled {
    }
}
