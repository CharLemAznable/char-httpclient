package com.github.charlemaznable.httpclient.ohclient.enhancer;

import net.sf.cglib.proxy.Callback;

public interface OhClientEnhancer {

    boolean isEnabled(Class<?> clientClass);

    Callback build(Class<?> clientClass, Object clientImpl);
}
