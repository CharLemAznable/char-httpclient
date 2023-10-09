package com.github.charlemaznable.httpclient.logging;

import lombok.val;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.GzipSource;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class LoggingOhInterceptor implements Interceptor {

    @Nonnull
    @Override
    public Response intercept(@Nonnull Interceptor.Chain chain) throws IOException {
        val request = chain.request();
        val logger = request.tag(Logger.class);
        if (isNull(logger) || !logger.isDebugEnabled()) return chain.proceed(request);

        val requestBody = request.body();
        val connection = chain.connection();

        String requestStartMessage = "--> " + request.method() + " " + request.url();
        if (nonNull(connection)) requestStartMessage += " " + connection.protocol();
        log(logger, requestStartMessage);

        val requestHeaders = request.headers();
        if (nonNull(requestBody)) {
            val contentType = requestBody.contentType();
            if (nonNull(contentType)) {
                if (isNull(requestHeaders.get("Content-Type"))) {
                    log(logger, "Content-Type: " + contentType);
                }
            }
            val contentLength = requestBody.contentLength();
            if (-1L != contentLength) {
                if (isNull(requestHeaders.get("Content-Length"))) {
                    log(logger, "Content-Length: " + contentLength);
                }
            }
        }
        for (int i = 0; i < requestHeaders.size(); i++) {
            logHeader(logger, requestHeaders, i);
        }
        if (isNull(requestBody)) {
            log(logger, "--> END " + request.method());
        } else if (bodyHasUnknownEncoding(requestHeaders)) {
            log(logger, "--> END " + request.method() + " (encoded body omitted)");
        } else if (requestBody.isDuplex()) {
            log(logger, "--> END " + request.method() + " (duplex request body omitted)");
        } else if (requestBody.isOneShot()) {
            log(logger, "--> END " + request.method() + " (one-shot body omitted)");
        } else {
            val buffer = new Buffer();
            requestBody.writeTo(buffer);

            val contentType = requestBody.contentType();
            val charset = checkNull(contentType, () -> UTF_8, t -> t.charset(UTF_8));

            log(logger, "");
            log(logger, buffer.readString(charset));
            log(logger, "--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
        }

        val startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log(logger, "<-- HTTP FAILED: " + e);
            throw e;
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        val responseBody = checkNotNull(response.body());
        log(logger, "<-- " + response.code()
                + (isEmpty(response.message()) ? "" : (" " + response.message()))
                + " " + response.request().url() + " (" + tookMs + "ms)");

        val responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            logHeader(logger, responseHeaders, i);
        }
        if (!HttpHeaders.promisesBody(response)) {
            log(logger, "<-- END HTTP");
        } else if (bodyHasUnknownEncoding(responseHeaders)) {
            log(logger, "<-- END HTTP (encoded body omitted)");
        } else {
            val source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.getBuffer();

            Long gzippedLength = null;
            if ("gzip".equalsIgnoreCase(responseHeaders.get("Content-Encoding"))) {
                gzippedLength = buffer.size();
                val gzippedResponseBody = new GzipSource(buffer.clone());
                buffer = new Buffer();
                buffer.writeAll(gzippedResponseBody);
            }

            val contentType = responseBody.contentType();
            val charset = checkNull(contentType, () -> UTF_8, t -> t.charset(UTF_8));

            if (responseBody.contentLength() != 0L) {
                log(logger, "");
                log(logger, buffer.clone().readString(charset));
            }

            if (nonNull(gzippedLength)) {
                log(logger, "<-- END HTTP (" + buffer.size() + "-byte, " + gzippedLength + "-gzipped-byte body)");
            } else {
                log(logger, "<-- END HTTP (" + buffer.size() + "-byte body)");
            }
        }
        return response;
    }

    private void logHeader(Logger logger, Headers headers, int i) {
        log(logger, headers.name(i) + ": " + headers.value(i));
    }

    private boolean bodyHasUnknownEncoding(Headers headers) {
        val contentEncoding = headers.get("Content-Encoding");
        if (isNull(contentEncoding)) return false;
        return !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }

    private void log(Logger logger, String content) {
        logger.debug(content);
    }
}
