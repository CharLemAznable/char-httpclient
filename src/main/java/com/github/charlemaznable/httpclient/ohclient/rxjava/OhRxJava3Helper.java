package com.github.charlemaznable.httpclient.ohclient.rxjava;

import io.reactivex.rxjava3.core.Single;
import lombok.NoArgsConstructor;

import java.util.concurrent.Future;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OhRxJava3Helper {

    public static Single<Object> buildSingle(Future<Object> future) {
        return Single.fromFuture(future);
    }
}
