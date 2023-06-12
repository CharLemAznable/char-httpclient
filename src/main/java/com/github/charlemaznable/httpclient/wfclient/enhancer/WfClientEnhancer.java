package com.github.charlemaznable.httpclient.wfclient.enhancer;

public interface WfClientEnhancer {

    boolean isEnabled(Class<?> clientClass);

    Object build(Class<?> clientClass, Object clientImpl);

    default int getOrder() {
        return 0;
    }
}
