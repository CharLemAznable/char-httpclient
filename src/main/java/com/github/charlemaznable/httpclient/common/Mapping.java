package com.github.charlemaznable.httpclient.common;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {

    @AliasFor("urls")
    String[] value() default "";

    @AliasFor("value")
    String[] urls() default "";

    Class<? extends UrlProvider> urlProvider() default UrlProvider.class;

    interface UrlProvider {

        default String url(Class<?> clazz) {
            throw new ProviderException(this.getClass().getName()
                    + "#url(Class<?>) need be overwritten");
        }

        default String url(Class<?> clazz, Method method) {
            throw new ProviderException(this.getClass().getName()
                    + "#url(Class<?>, Method) need be overwritten");
        }

        default List<String> urls(Class<?> clazz) {
            return newArrayList();
        }

        default List<String> urls(Class<?> clazz, Method method) {
            return newArrayList();
        }
    }
}
