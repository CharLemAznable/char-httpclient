package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.core.lang.function.FunctionWithException;
import lombok.NoArgsConstructor;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

import static com.github.charlemaznable.core.lang.function.Unchecker.unchecked;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Utils {

    public static Dispatcher dispatcher(FunctionWithException<RecordedRequest, MockResponse> function) {
        return new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) {
                return unchecked(function).apply(request);
            }
        };
    }
}
