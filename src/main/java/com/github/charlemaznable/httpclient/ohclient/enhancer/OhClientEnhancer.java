package com.github.charlemaznable.httpclient.ohclient.enhancer;

public interface OhClientEnhancer {

    boolean isEnabled(Class<?> clientClass);

    Object build(Class<?> clientClass, Object clientImpl);

    default int getOrder() {
        return 0;
    }
}
