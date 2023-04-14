package com.github.charlemaznable.httpclient.logging;

import lombok.val;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.GzipSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class LoggingOhInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        val request = chain.request();
        val logger = request.tag(Logger.class);
        if (isNull(logger) || !logger.isDebugEnabled()) return chain.proceed(request);

        val requestBody = request.body();
        val connection = chain.connection();

        String requestStartMessage = "--> " + request.method() + " " + request.url();
        if (nonNull(connection)) requestStartMessage += " " + connection.protocol();
        logger.debug(requestStartMessage);

        val requestHeaders = request.headers();
        if (nonNull(requestBody)) {
            val contentType = requestBody.contentType();
            if (nonNull(contentType)) {
                if (isNull(requestHeaders.get("Content-Type"))) {
                    logger.debug("Content-Type: " + contentType);
                }
            }
            val contentLength = requestBody.contentLength();
            if (-1L != contentLength) {
                if (isNull(requestHeaders.get("Content-Length"))) {
                    logger.debug("Content-Length: " + contentLength);
                }
            }
        }
        for (int i = 0; i < requestHeaders.size(); i++) {
            logHeader(logger, requestHeaders, i);
        }
        if (isNull(requestBody)) {
            logger.debug("--> END " + request.method());
        } else if (bodyHasUnknownEncoding(requestHeaders)) {
            logger.debug("--> END " + request.method() + " (encoded body omitted)");
        } else if (requestBody.isDuplex()) {
            logger.debug("--> END " + request.method() + " (duplex request body omitted)");
        } else if (requestBody.isOneShot()) {
            logger.debug("--> END " + request.method() + " (one-shot body omitted)");
        } else {
            val buffer = new Buffer();
            requestBody.writeTo(buffer);

            val contentType = requestBody.contentType();
            val charset = checkNull(contentType, () -> UTF_8, t -> t.charset(UTF_8));

            logger.debug("");
            logger.debug(buffer.readString(charset));
            logger.debug("--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
        }

        val startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.debug("<-- HTTP FAILED: " + e);
            throw e;
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        val responseBody = checkNotNull(response.body());
        logger.debug("<-- " + response.code()
                + (isEmpty(response.message()) ? "" : (" " + response.message()))
                + " " + response.request().url() + " (" + tookMs + "ms)");

        val responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            logHeader(logger, responseHeaders, i);
        }
        if (!HttpHeaders.promisesBody(response)) {
            logger.debug("<-- END HTTP");
        } else if (bodyHasUnknownEncoding(responseHeaders)) {
            logger.debug("<-- END HTTP (encoded body omitted)");
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
                logger.debug("");
                logger.debug(buffer.clone().readString(charset));
            }

            if (nonNull(gzippedLength)) {
                logger.debug("<-- END HTTP (" + buffer.size() + "-byte, " + gzippedLength + "-gzipped-byte body)");
            } else {
                logger.debug("<-- END HTTP (" + buffer.size() + "-byte body)");
            }
        }
        return response;
    }

    private void logHeader(Logger logger, Headers headers, int i) {
        logger.debug(headers.name(i) + ": " + headers.value(i));
    }

    private boolean bodyHasUnknownEncoding(Headers headers) {
        val contentEncoding = headers.get("Content-Encoding");
        if (isNull(contentEncoding)) return false;
        return !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }
}
