package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.httpclient.common.CommonMethod;
import com.github.charlemaznable.httpclient.rxjava.RxJavaHelper;
import lombok.val;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

final class OhMethod extends CommonMethod<OhBase> {

    boolean returnCoreFuture;
    boolean returnRxJavaSingle;
    boolean returnRxJava2Single;
    boolean returnRxJava3Single;

    public OhMethod(OhClass ohClass, Method method) {
        super(new OhElement(ohClass.element().factory()), ohClass, method);
        initialize();
    }

    @Override
    protected boolean checkReturnFuture(Class<?> returnType) {
        returnCoreFuture = Future.class == returnType;
        returnRxJavaSingle = RxJavaHelper.checkReturnRxJavaSingle(returnType);
        returnRxJava2Single = RxJavaHelper.checkReturnRxJava2Single(returnType);
        returnRxJava3Single = RxJavaHelper.checkReturnRxJava3Single(returnType);
        return returnCoreFuture || returnRxJavaSingle || returnRxJava2Single || returnRxJava3Single;
    }

    @Override
    public Object execute(Object[] args) {
        val ohExecute = new OhExecute(this);
        ohExecute.prepareArguments(args);
        return ohExecute.execute();
    }
}
