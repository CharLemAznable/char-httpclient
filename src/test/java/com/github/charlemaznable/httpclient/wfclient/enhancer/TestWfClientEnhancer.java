package com.github.charlemaznable.httpclient.wfclient.enhancer;

import com.google.auto.service.AutoService;

@AutoService(WfClientEnhancer.class)
public class TestWfClientEnhancer implements WfClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return true;
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return clientImpl;
    }
}
