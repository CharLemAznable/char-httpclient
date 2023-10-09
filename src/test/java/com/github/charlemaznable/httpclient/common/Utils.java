package com.github.charlemaznable.httpclient.common;

import lombok.NoArgsConstructor;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jooq.lambda.fi.util.function.CheckedFunction;

import javax.annotation.Nonnull;

import static lombok.AccessLevel.PRIVATE;
import static org.jooq.lambda.Sneaky.function;

@NoArgsConstructor(access = PRIVATE)
public final class Utils {

    public static Dispatcher dispatcher(CheckedFunction<RecordedRequest, MockResponse> checkedFunction) {
        return new Dispatcher() {
            @Nonnull
            @Override
            public MockResponse dispatch(@Nonnull RecordedRequest request) {
                return function(checkedFunction).apply(request);
            }
        };
    }
}
