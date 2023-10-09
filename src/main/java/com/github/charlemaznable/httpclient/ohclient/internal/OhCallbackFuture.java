package com.github.charlemaznable.httpclient.ohclient.internal;

import lombok.AllArgsConstructor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@AllArgsConstructor
public final class OhCallbackFuture<T> extends CompletableFuture<T> implements Callback {

    @Nonnull
    private Function<Response, T> function;

    @Override
    public void onFailure(@Nonnull Call call, @Nonnull IOException e) {
        super.completeExceptionally(e);
    }

    @Override
    public void onResponse(@Nonnull Call call, @Nonnull Response response) {
        try {
            super.complete(function.apply(response));
        } catch (Exception e) {
            super.completeExceptionally(e);
        }
    }
}
