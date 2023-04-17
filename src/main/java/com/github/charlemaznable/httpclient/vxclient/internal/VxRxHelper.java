package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.core.lang.ClzPath;
import lombok.NoArgsConstructor;
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

    static rx.Subscriber<Object> nullRxSubscriber() {
        return HAS_RXJAVA ? RxSubscriberHolder.NULL : null;
    }

    static io.reactivex.SingleObserver<Object> nullRx2Observer() {
        return HAS_RXJAVA2 ? Rx2ObserverHolder.NULL : null;
    }

    static io.reactivex.rxjava3.core.SingleObserver<Object> nullRx3Observer() {
        return HAS_RXJAVA3 ? Rx3ObserverHolder.NULL : null;
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
