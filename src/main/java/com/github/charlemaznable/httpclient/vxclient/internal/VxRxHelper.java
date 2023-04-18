package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.core.lang.ClzPath;
import io.vertx.core.Future;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class VxRxHelper {

    static final boolean HAS_RXJAVA =
            ClzPath.classExists("io.vertx.rxjava.core.Vertx");
    static final boolean HAS_RXJAVA2 =
            ClzPath.classExists("io.vertx.reactivex.core.Vertx");
    static final boolean HAS_RXJAVA3 =
            ClzPath.classExists("io.vertx.rxjava3.core.Vertx");

    static boolean checkReturnRxJavaSingle(Class<?> returnType) {
        if (!HAS_RXJAVA) return false;
        return rx.Single.class == returnType;
    }

    static boolean checkReturnRxJava2Single(Class<?> returnType) {
        if (!HAS_RXJAVA2) return false;
        return io.reactivex.Single.class == returnType;
    }

    static boolean checkReturnRxJava3Single(Class<?> returnType) {
        if (!HAS_RXJAVA3) return false;
        return io.reactivex.rxjava3.core.Single.class == returnType;
    }

    static rx.Single<Object> buildRxSingle(Future<Object> future) {
        val single = rx.Single.create(sub -> future.onComplete(ar -> {
            if (!sub.isUnsubscribed()) {
                if (ar.succeeded()) {
                    sub.onSuccess(ar.result());
                } else {
                    sub.onError(ar.cause());
                }
            }
        }));
        single.subscribe(RxSubscriberHolder.NULL);
        return single;
    }

    static io.reactivex.Single<Object> buildRxSingle2(Future<Object> future) {
        val single = io.vertx.reactivex.impl.AsyncResultSingle.toSingle(future::onComplete);
        single.subscribe(Rx2ObserverHolder.NULL);
        return single;
    }

    static io.reactivex.rxjava3.core.Single<Object> buildRxSingle3(Future<Object> future) {
        val single = io.vertx.rxjava3.impl.AsyncResultSingle.toSingle(future::onComplete);
        single.subscribe(Rx3ObserverHolder.NULL);
        return single;
    }

    private static class RxSubscriberHolder {

        private static final rx.Subscriber<Object> NULL =
                new rx.Subscriber<>() {
                    @Override
                    public void onCompleted() {
                        // empty method
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // empty method
                    }

                    @Override
                    public void onNext(Object o) {
                        // empty method
                    }
                };
    }

    private static class Rx2ObserverHolder {

        private static final io.reactivex.SingleObserver<Object> NULL =
                new io.reactivex.SingleObserver<>() {
                    @Override
                    public void onSubscribe(@NotNull io.reactivex.disposables.Disposable disposable) {
                        // empty method
                    }

                    @Override
                    public void onSuccess(@NotNull Object o) {
                        // empty method
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        // empty method
                    }
                };
    }

    private static class Rx3ObserverHolder {

        private static final io.reactivex.rxjava3.core.SingleObserver<Object> NULL =
                new io.reactivex.rxjava3.core.SingleObserver<>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull io.reactivex.rxjava3.disposables.Disposable d) {
                        // empty method
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Object o) {
                        // empty method
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        // empty method
                    }
                };
    }
}
