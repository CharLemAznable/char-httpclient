package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.httpclient.common.CommonMethod;
import io.vertx.core.Future;
import lombok.val;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Clz.isAssignable;

final class VxMethod extends CommonMethod<VxBase> {

    boolean returnCoreFuture;

    public VxMethod(VxClass vxClass, Method method) {
        super(new VxElement(vxClass.element().base().vertx,
                vxClass.element().factory()), vxClass, method);
        initialize();
    }

    @Override
    protected boolean checkReturnFuture(Class<?> returnType) {
        val superReturnFuture = super.checkReturnFuture(returnType);
        returnCoreFuture = checkReturnCoreFuture(returnType);
        if (!superReturnFuture && !returnCoreFuture) {
            throw new IllegalStateException(method().getName() +
                    " must return io.vertx.core.Future<?>[io.vertx:vertx-core]" +
                    " or java.util.concurrent.Future<?>" +
                    " or reactor.core.publisher.Mono<?>[io.projectreactor:reactor-core]" +
                    " or rx.Single<?>[io.reactivex:rxjava]" +
                    " or io.reactivex.Single<?>[io.reactivex.rxjava2:rxjava]" +
                    " or io.reactivex.rxjava3.core.Single<?>[io.reactivex.rxjava3:rxjava]" +
                    " or io.smallrye.mutiny.Uni<?>[io.smallrye.reactive:mutiny]");
        }
        return true;
    }

    private boolean checkReturnCoreFuture(Class<?> returnType) {
        return Object.class != returnType
                && isAssignable(Future.class, returnType);
    }

    @Override
    public Object execute(Object[] args) {
        val vxExecute = new VxExecute(this);
        vxExecute.prepareArguments(args);
        return vxExecute.execute();
    }
}
