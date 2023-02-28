package com.github.charlemaznable.httpclient.ohclient.internal;

import lombok.AllArgsConstructor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@AllArgsConstructor
public final class OhCallbackFuture<T> extends CompletableFuture<T> implements Callback {

    @Nonnull
    private Function<Response, T> function;

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        super.completeExceptionally(e);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) {
        try {
            super.complete(function.apply(response));
        } catch (Exception e) {
            super.completeExceptionally(e);
        }
    }
}
