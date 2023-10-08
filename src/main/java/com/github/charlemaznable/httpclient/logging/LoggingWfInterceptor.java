package com.github.charlemaznable.httpclient.logging;

import lombok.val;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.httpclient.common.HttpStatus.CONTINUE;
import static com.github.charlemaznable.httpclient.common.HttpStatus.NOT_MODIFIED;
import static com.github.charlemaznable.httpclient.common.HttpStatus.NO_CONTENT;
import static com.github.charlemaznable.httpclient.wfclient.elf.RequestSpecConfigElf.REQUEST_BODY_AS_STRING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class LoggingWfInterceptor implements ExchangeFilterFunction {

    @Nonnull
    @Override
    public Mono<ClientResponse> filter(@Nonnull ClientRequest request, @Nonnull ExchangeFunction next) {
        val loggerOptional = request.attribute(Logger.class.getName());
        if (loggerOptional.isEmpty()) return next.exchange(request);
        val logger = (Logger) loggerOptional.get();
        if (!logger.isDebugEnabled()) return next.exchange(request);

        log(logger, "--> " + request.method().name() + " " + request.url());

        val requestHeaders = request.headers();
        for (val name : requestHeaders.keySet()) {
            logHeader(logger, requestHeaders, name);
        }
        val requestBodyOptional = request.attribute(REQUEST_BODY_AS_STRING);
        if (requestBodyOptional.isEmpty()) {
            log(logger, "--> END " + request.method());
        } else {
            val requestBody = (String) requestBodyOptional.get();
            if (bodyHasUnknownEncoding(requestHeaders)) {
                log(logger, "--> END " + request.method() + " (encoded body omitted)");
            } else {
                val contentType = requestHeaders.getContentType();
                val charset = checkNull(contentType, () -> UTF_8, MimeType::getCharset);
                val requestLength = nullThen(bytes(requestBody, charset), () -> new byte[0]).length;

                log(logger, "");
                log(logger, requestBody);
                log(logger, "--> END " + request.method() + " (" + requestLength + "-byte body)");
            }
        }

        val startNs = System.nanoTime();
        return next.exchange(request).flatMap(clientResponse -> {
            val builder = clientResponse.mutate();
            return clientResponse.toEntity(String.class).map(responseEntity -> {
                val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

                val statusCode = responseEntity.getStatusCode().value();
                val statusMessage = checkNull(HttpStatus.resolve(statusCode),
                        () -> "", s -> " " + s.getReasonPhrase());
                log(logger, "<-- " + statusCode + statusMessage
                        + " " + request.url() + " (" + tookMs + "ms)");

                val responseHeaders = responseEntity.getHeaders();
                for (val name : responseHeaders.keySet()) {
                    logHeader(logger, responseHeaders, name);
                }

                val responseBody = responseEntity.getBody();
                if (!promisesBody(request.method(), responseEntity)) {
                    log(logger, "<-- END HTTP");
                } else if (bodyHasUnknownEncoding(responseHeaders)) {
                    log(logger, "<-- END HTTP (encoded body omitted)");
                } else {
                    val contentType = responseHeaders.getContentType();
                    val charset = checkNull(contentType, () -> UTF_8, MimeType::getCharset);
                    val responseLength = nullThen(bytes(responseBody, charset), () -> new byte[0]).length;

                    if (responseLength != 0L) {
                        log(logger, "");
                        log(logger, responseBody);
                    }
                    log(logger, "<-- END HTTP (" + responseLength + "-byte body)");
                }
                if (nonNull(responseBody)) builder.body(responseBody);
                return builder.build();
            });
        }).doOnError(throwable -> log(logger, "<-- HTTP FAILED: " + throwable));
    }

    private void logHeader(Logger logger, HttpHeaders headers, String name) {
        log(logger, name + ": " + String.join(", ", headers.getOrEmpty(name)));
    }

    private boolean bodyHasUnknownEncoding(HttpHeaders headers) {
        val contentEncoding = headers.getFirst("Content-Encoding");
        if (isNull(contentEncoding)) return false;
        return !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }

    private boolean promisesBody(HttpMethod httpMethod, ResponseEntity<?> response) {
        // HEAD requests never yield a body regardless of the response headers.
        if (httpMethod == HttpMethod.HEAD) {
            return false;
        }

        val responseCode = response.getStatusCode().value();
        if ((responseCode < CONTINUE.value() || responseCode >= 200) &&
                responseCode != NO_CONTENT.value() &&
                responseCode != NOT_MODIFIED.value()) {
            return true;
        }

        // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
        // response is malformed. For best compatibility, we honor the headers.
        return response.getHeaders().getContentLength() != -1L ||
                "chunked".equalsIgnoreCase(response.getHeaders().getFirst("Transfer-Encoding"));
    }

    private void log(Logger logger, String content) {
        logger.debug(content);
    }
}
