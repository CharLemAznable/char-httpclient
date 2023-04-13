package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.httpclient.common.CommonMethod;
import io.vertx.core.Future;
import lombok.val;

import java.lang.reflect.Method;

final class VxMethod extends CommonMethod<VxBase> {

    public VxMethod(VxClass vxClass, Method method) {
        super(new VxElement(vxClass.element().base().vertx,
                vxClass.element().factory()), vxClass, method);
        initialize();
    }

    @Override
    protected boolean checkReturnFuture(Class<?> returnType) {
        val returnFuture = Future.class == returnType;
        if (!returnFuture) {
            throw new IllegalStateException(method().getName()
                    + " must return io.vertx.core.Future<?>");
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
