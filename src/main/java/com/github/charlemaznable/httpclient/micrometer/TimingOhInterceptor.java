package com.github.charlemaznable.httpclient.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.http.Outcome;
import lombok.AllArgsConstructor;
import lombok.Lombok;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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
        val meterRegistry = request.tag(MeterRegistry.class);
        if (isNull(meterRegistry)) return chain.proceed(request);

        val startTime = meterRegistry.config().clock().monotonicTime();
        val callState = new CallState(startTime, request);
        try {
            callState.response = chain.proceed(request);
        } catch (Exception e) {
            callState.exception = e;
        }
        timing(callState, meterRegistry);
        return callState.proceed();
    }

    private void timing(CallState state, MeterRegistry registry) {
        Timer.builder(this.metricName).description("Timer of OhClient operation")
                .tags(Tags.of(generateTagsForRequest(state.request))
                        .and(generateTagsForRoute(state.request))
                        .and(generateStatusTags(state))
                        .and(getStatusOutcomeTag(state.response))
                        .and(getRequestTags(state.request))).register(registry)
                .record(registry.config().clock().monotonicTime() - state.startTime, TimeUnit.NANOSECONDS);
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

    private Tags generateStatusTags(CallState state) {
        return Tags.of("status", getStatusMessage(state));
    }

    private String getStatusMessage(CallState state) {
        if (nonNull(state.exception)) return "IO_ERROR";
        if (isNull(state.response)) return "CLIENT_ERROR";
        return Integer.toString(state.response.code());
    }

    private Tag getStatusOutcomeTag(Response response) {
        if (isNull(response)) return Outcome.UNKNOWN.asTag();
        return Outcome.forStatus(response.code()).asTag();
    }

    private Tags getRequestTags(Request request) {
        val requestTag = request.tag(Tags.class);
        return requireNonNullElse(requestTag, Tags.empty());
    }

    @RequiredArgsConstructor
    private static class CallState {

        final long startTime;
        final Request request;
        Response response;
        Exception exception;

        @Nonnull
        Response proceed() {
            if (nonNull(exception)) {
                throw Lombok.sneakyThrow(exception);
            }
            return response;
        }
    }
}
