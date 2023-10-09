package com.github.charlemaznable.httpclient.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.http.Outcome;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.HttpContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.concurrent.TimeUnit;

import static io.vertx.ext.web.client.impl.ClientPhase.CREATE_REQUEST;
import static io.vertx.ext.web.client.impl.ClientPhase.DISPATCH_RESPONSE;
import static io.vertx.ext.web.client.impl.ClientPhase.FAILURE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;

@NoArgsConstructor
@AllArgsConstructor
public final class TimingVxInterceptor implements Handler<HttpContext<?>> {

    private static final String START_TIME = "timing.startTime";

    private String metricName = "default.vxclient.timer";

    @SuppressWarnings("unchecked")
    @Override
    public void handle(HttpContext<?> httpContext) {
        val registry = httpContext.<MeterRegistry>get(MeterRegistry.class.getName());
        if (isNull(registry)) {
            httpContext.next();
            return;
        }
        if (CREATE_REQUEST == httpContext.phase()) {
            handleStart((HttpContext<Buffer>) httpContext, registry);
        } else if (DISPATCH_RESPONSE == httpContext.phase() || FAILURE == httpContext.phase()) {
            handleFinish((HttpContext<Buffer>) httpContext, registry);
        }
        httpContext.next();
    }

    private void handleStart(HttpContext<Buffer> httpContext, MeterRegistry registry) {
        httpContext.set(START_TIME, registry.config().clock().monotonicTime());
    }

    private void handleFinish(HttpContext<Buffer> httpContext, MeterRegistry registry) {
        val startTime = httpContext.<Long>get(START_TIME);
        if (isNull(startTime)) return;

        Timer.builder(this.metricName).description("Timer of VxClient operation")
                .tags(Tags.of(generateTagsForRequest(httpContext.request()))
                        .and(generateTagsForRoute(httpContext.request()))
                        .and(generateStatusTags(httpContext))
                        .and(getStatusOutcomeTag(httpContext.response()))
                        .and(getContextTags(httpContext))).register(registry)
                .record(registry.config().clock().monotonicTime() - startTime, TimeUnit.NANOSECONDS);
    }

    private Tags generateTagsForRequest(HttpRequest<?> request) {
        return Tags.of(
                "method", request.method().name(),
                "host", request.host(),
                "uri", request.uri());
    }

    private Tags generateTagsForRoute(HttpRequest<?> request) {
        return Tags.of(
                "target.host", request.host(),
                "target.port", Integer.toString(request.port()));
    }

    private Tags generateStatusTags(HttpContext<Buffer> httpContext) {
        return Tags.of("status", getStatusMessage(httpContext));
    }

    private String getStatusMessage(HttpContext<Buffer> httpContext) {
        if (nonNull(httpContext.failure())) return "IO_ERROR";
        if (isNull(httpContext.response())) return "CLIENT_ERROR";
        return Integer.toString(httpContext.response().statusCode());
    }

    private Tag getStatusOutcomeTag(HttpResponse<?> response) {
        if (isNull(response)) return Outcome.UNKNOWN.asTag();
        return Outcome.forStatus(response.statusCode()).asTag();
    }

    private Tags getContextTags(HttpContext<Buffer> httpContext) {
        return requireNonNullElse(httpContext.get(Tags.class.getName()), Tags.empty());
    }
}
