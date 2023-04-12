package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CommonClass;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.github.charlemaznable.httpclient.ohclient.OhException;

import java.lang.reflect.Method;

import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

public final class OhClass extends CommonClass<OhBase> implements BuddyEnhancer.Delegate {

    public OhClass(Factory factory, Class<?> clazz) {
        super(new OhElement(factory), OhBase.DEFAULT, clazz);
        checkOhClient();
        initialize();
    }

    private void checkOhClient() {
        if (isAnnotated(clazz(), OhClient.class)) return;
        throw new OhException(clazz().getName() + " has no OhClient annotation");
    }

    @Override
    protected OhMethod loadMethod(Method method) {
        return new OhMethod(this, method);
    }

    @Override
    public Object invoke(BuddyEnhancer.Invocation invocation) throws Exception {
        return execute(invocation.getMethod(), invocation.getArguments());
    }
}
