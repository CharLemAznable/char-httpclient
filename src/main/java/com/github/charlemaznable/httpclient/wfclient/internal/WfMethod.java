package com.github.charlemaznable.httpclient.wfclient.internal;

import com.github.charlemaznable.httpclient.common.CommonMethod;
import lombok.val;

import java.lang.reflect.Method;

final class WfMethod extends CommonMethod<WfBase> {

    public WfMethod(WfClass wfClass, Method method) {
        super(new WfElement(wfClass.element().factory()), wfClass, method);
        initialize();
    }

    @Override
    protected boolean checkReturnFuture(Class<?> returnType) {
        if (!super.checkReturnFuture(returnType)) {
            throw new IllegalStateException(method().getName() +
                    " must return java.util.concurrent.Future<?>" +
                    " or reactor.core.publisher.Mono<?>[io.projectreactor:reactor-core]" +
                    " or rx.Single<?>[io.reactivex:rxjava]" +
                    " or io.reactivex.Single<?>[io.reactivex.rxjava2:rxjava]" +
                    " or io.reactivex.rxjava3.core.Single<?>[io.reactivex.rxjava3:rxjava]" +
                    " or io.smallrye.mutiny.Uni<?>[io.smallrye.reactive:mutiny]");
        }
        return true;
    }

    @Override
    public Object execute(Object[] args) {
        val wfExecute = new WfExecute(this);
        wfExecute.prepareArguments(args);
        return wfExecute.execute();
    }
}
