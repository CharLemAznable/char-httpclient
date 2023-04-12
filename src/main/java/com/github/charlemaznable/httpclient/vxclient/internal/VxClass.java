package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CommonClass;
import com.github.charlemaznable.httpclient.vxclient.VxClient;
import com.github.charlemaznable.httpclient.vxclient.VxException;
import io.vertx.core.Vertx;

import java.lang.reflect.Method;

import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

public final class VxClass extends CommonClass<VxBase> implements BuddyEnhancer.Delegate {

    public VxClass(Vertx vertx, Factory factory, Class<?> clazz) {
        super(new VxElement(vertx, factory), VxBase.defaultBase(vertx), clazz);
        checkVxClient();
        initialize();
    }

    private void checkVxClient() {
        if (isAnnotated(clazz(), VxClient.class)) return;
        throw new VxException(clazz().getName() + " has no VxClient annotation");
    }

    @Override
    protected VxMethod loadMethod(Method method) {
        return new VxMethod(this, method);
    }

    @Override
    public Object invoke(BuddyEnhancer.Invocation invocation) throws Exception {
        return execute(invocation.getMethod(), invocation.getArguments());
    }
}
