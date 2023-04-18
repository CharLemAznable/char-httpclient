package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.httpclient.common.CommonMethod;
import io.vertx.core.Future;
import lombok.val;

import java.lang.reflect.Method;

final class VxMethod extends CommonMethod<VxBase> {

    boolean returnCoreFuture;
    boolean returnRxJavaSingle;
    boolean returnRxJava2Single;
    boolean returnRxJava3Single;

    public VxMethod(VxClass vxClass, Method method) {
        super(new VxElement(vxClass.element().base().vertx,
                vxClass.element().factory()), vxClass, method);
        initialize();
    }

    @Override
    protected boolean checkReturnFuture(Class<?> returnType) {
        returnCoreFuture = Future.class == returnType;
        returnRxJavaSingle = VxRxHelper.checkReturnRxJavaSingle(returnType);
        returnRxJava2Single = VxRxHelper.checkReturnRxJava2Single(returnType);
        returnRxJava3Single = VxRxHelper.checkReturnRxJava3Single(returnType);
        if (!returnCoreFuture && !returnRxJavaSingle && !returnRxJava2Single && !returnRxJava3Single) {
            throw new IllegalStateException(method().getName() +
                    " must return io.vertx.core.Future<?>[io.vertx:vertx-core]" +
                    " or rx.Single<?>[io.reactivex:rxjava]" +
                    " or io.reactivex.Single<?>[io.reactivex.rxjava2:rxjava]" +
                    " or io.reactivex.rxjava3.core.Single<?>[io.reactivex.rxjava3:rxjava]");
        }
        return true;
    }

    @Override
    public Object execute(Object[] args) {
        val vxExecute = new VxExecute(this);
        vxExecute.prepareArguments(args);
        return vxExecute.execute();
    }
}
