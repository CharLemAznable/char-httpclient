package com.github.charlemaznable.httpclient.ohclient.westcache;

import com.github.charlemaznable.httpclient.ohclient.enhancer.OhClientEnhancer;
import com.google.auto.service.AutoService;

@AutoService(OhClientEnhancer.class)
public class TestOhClientEnhancer implements OhClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return false;
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return null;
    }
}
