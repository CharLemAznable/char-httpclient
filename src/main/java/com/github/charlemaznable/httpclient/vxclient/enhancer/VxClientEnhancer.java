package com.github.charlemaznable.httpclient.vxclient.enhancer;

public interface VxClientEnhancer {

    boolean isEnabled(Class<?> clientClass);

    Object build(Class<?> clientClass, Object clientImpl);

    default int getOrder() {
        return 0;
    }
}
