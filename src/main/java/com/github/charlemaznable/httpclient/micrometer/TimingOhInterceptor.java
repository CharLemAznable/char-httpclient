package com.github.charlemaznable.httpclient.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.http.Outcome;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;

@NoArgsConstructor
@AllArgsConstructor
public final class TimingOhInterceptor implements Interceptor {

    private String metricName = "default.ohclient.timer";

    @Nonnull
    @Override
    public Response intercept(@Nonnull Interceptor.Chain chain) throws IOException {
        val request = chain.request();
        val registry = request.tag(MeterRegistry.class);
        if (isNull(registry)) return chain.proceed(request);

        val startTime = registry.config().clock().monotonicTime();
        Response response = null;
        IOException exception = null;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            exception = e;
        }

        Timer.builder(this.metricName).description("Timer of OhClient operation")
                .tags(Tags.of(generateTagsForRequest(request))
                        .and(generateTagsForRoute(request))
                        .and(generateStatusTags(response, exception))
                        .and(getStatusOutcomeTag(response))
                        .and(getRequestTags(request))).register(registry)
                .record(registry.config().clock().monotonicTime() - startTime, TimeUnit.NANOSECONDS);

        if (nonNull(exception)) throw exception;
        return response;
    }

    private Tags generateTagsForRequest(Request request) {
        return Tags.of(
                "method", request.method(),
                "host", request.url().host(),
                "uri", request.url().encodedPath());
    }

    private Tags generateTagsForRoute(Request request) {
        return Tags.of(
                "target.scheme", request.url().scheme(),
                "target.host", request.url().host(),
                "target.port", Integer.toString(request.url().port()));
    }

    private Tags generateStatusTags(Response response, Throwable throwable) {
        return Tags.of("status", getStatusMessage(response, throwable));
    }

    private String getStatusMessage(Response response, Throwable throwable) {
        if (nonNull(throwable)) return "IO_ERROR";
        if (isNull(response)) return "CLIENT_ERROR";
        return Integer.toString(response.code());
    }

    private Tag getStatusOutcomeTag(Response response) {
        if (isNull(response)) return Outcome.UNKNOWN.asTag();
        return Outcome.forStatus(response.code()).asTag();
    }

    private Tags getRequestTags(Request request) {
        return requireNonNullElse(request.tag(Tags.class), Tags.empty());
    }
}
