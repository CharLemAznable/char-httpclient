package com.github.charlemaznable.httpclient.vxclient.rxjava;

import io.vertx.core.Future;
import lombok.NoArgsConstructor;
import rx.Single;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class VxRxJavaHelper {

    public static Single<Object> buildSingle(Future<Object> future) {
        return Single.create(sub -> future.onComplete(ar -> {
            if (!sub.isUnsubscribed()) {
                if (ar.succeeded()) {
                    sub.onSuccess(ar.result());
                } else {
                    sub.onError(ar.cause());
                }
            }
        }));
    }
}
