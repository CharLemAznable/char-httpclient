package com.github.charlemaznable.httpclient.ohclient.rxjava;

import lombok.NoArgsConstructor;
import rx.Single;

import java.util.concurrent.Future;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OhRxJavaHelper {

    public static Single<Object> buildSingle(Future<Object> future) {
        return Single.from(future);
    }
}
