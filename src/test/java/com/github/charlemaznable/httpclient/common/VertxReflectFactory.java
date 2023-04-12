package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.core.lang.Clz;
import com.github.charlemaznable.core.lang.Factory;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;

import static com.github.charlemaznable.core.lang.Clz.isConcrete;
import static org.joor.Reflect.onClass;

@AllArgsConstructor
public class VertxReflectFactory implements Factory {

    private Vertx vertx;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T build(Class<T> clazz) {
        if (Clz.isAssignable(clazz, Vertx.class)) return (T) vertx;
        if (!isConcrete(clazz)) return null;
        return onClass(clazz).create().get();
    }
}
