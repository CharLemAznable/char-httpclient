package com.github.charlemaznable.httpclient.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.http.Outcome;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@NoArgsConstructor
@AllArgsConstructor
public final class TimingWfInterceptor implements ExchangeFilterFunction {

    private String metricName = "default.wfclient.timer";

    @Nonnull
    @Override
    public Mono<ClientResponse> filter(@Nonnull ClientRequest request, @Nonnull ExchangeFunction next) {
        val registryOptional = request.attribute(MeterRegistry.class.getName());
        if (registryOptional.isEmpty()) return next.exchange(request);

        val registry = (MeterRegistry) registryOptional.get();
        val startTime = registry.config().clock().monotonicTime();
        BiConsumer<ClientResponse, Throwable> consumer = (response, throwable) ->
                Timer.builder(this.metricName).description("Timer of OhClient operation")
                        .tags(Tags.of(generateTagsForRequest(request))
                                .and(generateTagsForRoute(request))
                                .and(generateStatusTags(response, throwable))
                                .and(getStatusOutcomeTag(response))
                                .and(getRequestTags(request))).register(registry)
                        .record(registry.config().clock().monotonicTime() - startTime, TimeUnit.NANOSECONDS);
        return next.exchange(request)
                .doOnSuccess(clientResponse -> consumer.accept(clientResponse, null))
                .doOnError(throwable -> consumer.accept(null, throwable));
    }

    private Tags generateTagsForRequest(ClientRequest request) {
        return Tags.of(
                "method", request.method().name(),
                "host", request.url().getHost(),
                "uri", request.url().getPath());
    }

    private Tags generateTagsForRoute(ClientRequest request) {
        return Tags.of(
                "target.scheme", request.url().getScheme(),
                "target.host", request.url().getHost(),
                "target.port", Integer.toString(request.url().getPort()));
    }

    private Tags generateStatusTags(ClientResponse response, Throwable throwable) {
        return Tags.of("status", getStatusMessage(response, throwable));
    }

    private String getStatusMessage(ClientResponse response, Throwable throwable) {
        if (nonNull(throwable)) return "IO_ERROR";
        if (isNull(response)) return "CLIENT_ERROR";
        return Integer.toString(response.statusCode().value());
    }

    private Tag getStatusOutcomeTag(ClientResponse response) {
        if (isNull(response)) return Outcome.UNKNOWN.asTag();
        return Outcome.forStatus(response.statusCode().value()).asTag();
    }

    private Tags getRequestTags(ClientRequest request) {
        return (Tags) request.attribute(Tags.class.getName()).orElseGet(Tags::empty);
    }
}
