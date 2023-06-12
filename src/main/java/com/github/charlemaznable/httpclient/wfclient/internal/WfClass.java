package com.github.charlemaznable.httpclient.wfclient.internal;

import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CommonClass;
import com.github.charlemaznable.httpclient.wfclient.WfClient;
import com.github.charlemaznable.httpclient.wfclient.WfException;

import java.lang.reflect.Method;

import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

public final class WfClass extends CommonClass<WfBase> implements BuddyEnhancer.Delegate {

    public WfClass(Factory factory, Class<?> clazz) {
        super(new WfElement(factory), WfBase.DEFAULT, clazz);
        checkWfClient();
        initialize();
    }

    private void checkWfClient() {
        if (isAnnotated(clazz(), WfClient.class)) return;
        throw new WfException(clazz().getName() + " has no WfClient annotation");
    }

    @Override
    protected WfMethod loadMethod(Method method) {
        return new WfMethod(this, method);
    }

    @Override
    public Object invoke(BuddyEnhancer.Invocation invocation) throws Exception {
        return execute(invocation.getMethod(), invocation.getArguments());
    }
}
