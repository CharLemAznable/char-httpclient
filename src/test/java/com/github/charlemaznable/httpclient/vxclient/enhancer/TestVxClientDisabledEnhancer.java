package com.github.charlemaznable.httpclient.vxclient.enhancer;

import com.google.auto.service.AutoService;

@AutoService(VxClientEnhancer.class)
public class TestVxClientDisabledEnhancer implements VxClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return false;
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return null;
    }
}
