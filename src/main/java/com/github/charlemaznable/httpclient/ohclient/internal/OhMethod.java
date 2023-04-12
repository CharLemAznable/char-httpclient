package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.httpclient.common.CommonMethod;
import lombok.val;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

final class OhMethod extends CommonMethod<OhBase> {

    public OhMethod(OhClass ohClass, Method method) {
        super(new OhElement(ohClass.element().factory()), ohClass, method);
        initialize();
    }

    @Override
    protected boolean checkReturnFuture(Class<?> returnType) {
        return Future.class == returnType;
    }

    @Override
    public Object execute(Object[] args) throws Exception {
        val ohExecute = new OhExecute(this);
        ohExecute.prepareArguments(args);
        return ohExecute.execute();
    }
}
