package com.github.charlemaznable.httpclient.vxclient.rxjava;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.vertx.core.Future;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.concurrent.atomic.AtomicBoolean;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class VxRxJava3Helper {

    @SuppressWarnings("DuplicatedCode")
    public static Single<Object> buildSingle(Future<Object> future) {
        return RxJavaPlugins.onAssembly(new Single<>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Object> observer) {
                val disposed = new AtomicBoolean();
                observer.onSubscribe(new Disposable() {
                    @Override
                    public void dispose() {
                        disposed.set(true);
                    }
                    @Override
                    public boolean isDisposed() {
                        return disposed.get();
                    }
                });
                if (!disposed.get()) {
                    try {
                        future.onComplete(ar -> {
                            if (!disposed.getAndSet(true)) {
                                if (ar.succeeded()) {
                                    try {
                                        observer.onSuccess(ar.result());
                                    } catch (Throwable t) {
                                        Exceptions.throwIfFatal(t);
                                        RxJavaPlugins.onError(t);
                                    }
                                } else if (ar.failed()) {
                                    try {
                                        observer.onError(ar.cause());
                                    } catch (Throwable t) {
                                        Exceptions.throwIfFatal(t);
                                        RxJavaPlugins.onError(t);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        if (!disposed.getAndSet(true)) {
                            try {
                                observer.onError(e);
                            } catch (Throwable t) {
                                Exceptions.throwIfFatal(t);
                                RxJavaPlugins.onError(t);
                            }
                        }
                    }
                }
            }
        });
    }
}
