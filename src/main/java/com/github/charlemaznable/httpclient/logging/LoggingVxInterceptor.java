package com.github.charlemaznable.httpclient.logging;

import com.github.charlemaznable.httpclient.common.CommonReq;
import com.github.charlemaznable.httpclient.vxclient.internal.VxExecuteRequest;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.HttpContext;
import lombok.val;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static com.github.charlemaznable.httpclient.common.HttpStatus.CONTINUE;
import static com.github.charlemaznable.httpclient.common.HttpStatus.NOT_MODIFIED;
import static com.github.charlemaznable.httpclient.common.HttpStatus.NO_CONTENT;
import static io.vertx.ext.web.client.impl.ClientPhase.CREATE_REQUEST;
import static io.vertx.ext.web.client.impl.ClientPhase.DISPATCH_RESPONSE;
import static io.vertx.ext.web.client.impl.ClientPhase.FAILURE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

public final class LoggingVxInterceptor implements Handler<HttpContext<?>> {

    private static final String START_NS = "logging.startNs";

    @SuppressWarnings("unchecked")
    @Override
    public void handle(HttpContext<?> httpContext) {
        val logger = httpContext.<Logger>get(Logger.class.getName());
        if (isNull(logger) || !logger.isDebugEnabled()) {
            httpContext.next();
            return;
        }
        if (CREATE_REQUEST == httpContext.phase()) {
            loggingCreateRequest((HttpContext<Buffer>) httpContext, logger);
        } else if (DISPATCH_RESPONSE == httpContext.phase()) {
            loggingDispatchResponse((HttpContext<Buffer>) httpContext, logger);
        } else if (FAILURE == httpContext.phase()) {
            loggingFailure((HttpContext<Buffer>) httpContext, logger);
        }
        httpContext.next();
    }

    private void loggingCreateRequest(HttpContext<Buffer> httpContext, Logger logger) {
        if (nonNull(httpContext.get(START_NS))) {
            val startNs = System.nanoTime();
            httpContext.set(START_NS, startNs);
            httpContext.next();
            return;
        }
        val executeRequest = httpContext.<VxExecuteRequest>get(VxExecuteRequest.class.getName());
        val requestOptions = httpContext.requestOptions();
        val request = httpContext.request();
        val requestBody = (Buffer) httpContext.body();

        logger.debug("--> " + requestOptions.getMethod() + " "
                + checkNull(executeRequest, () -> "unknown url", VxExecuteRequest::getRequestUrl));

        val requestHeaders = request.headers();
        for (val name : requestHeaders.names()) {
            logHeader(logger, requestHeaders, name);
        }
        if (isNull(requestBody)) {
            logger.debug("--> END " + requestOptions.getMethod());
        } else if (bodyHasUnknownEncoding(requestHeaders)) {
            logger.debug("--> END " + requestOptions.getMethod() + " (encoded body omitted)");
        } else {
            val contentType = requestHeaders.get("Content-Type");
            val charset = checkNull(contentType, UTF_8::name, CommonReq::parseCharset);

            logger.debug("");
            logger.debug(requestBody.toString(charset));
            logger.debug("--> END " + requestOptions.getMethod() + " (" + requestBody.length() + "-byte body)");
        }

        val startNs = System.nanoTime();
        httpContext.set(START_NS, startNs);
    }

    private void loggingDispatchResponse(HttpContext<Buffer> httpContext, Logger logger) {
        val startNs = httpContext.<Long>get(START_NS);
        val tookMs = isNull(startNs) ? -1L : TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        val executeRequest = httpContext.<VxExecuteRequest>get(VxExecuteRequest.class.getName());
        val requestOptions = httpContext.requestOptions();
        val response = httpContext.response();
        logger.debug("<-- " + response.statusCode()
                + (isEmpty(response.statusMessage()) ? "" : (" " + response.statusMessage()))
                + " " + checkNull(executeRequest, () -> "unknown url", VxExecuteRequest::getRequestUrl)
                + " (" + tookMs + "ms)");

        val responseHeaders = response.headers();
        for (val name : responseHeaders.names()) {
            logHeader(logger, responseHeaders, name);
        }
        if (!promisesBody(requestOptions, response)) {
            logger.debug("<-- END HTTP");
        } else if (bodyHasUnknownEncoding(responseHeaders)) {
            logger.debug("<-- END HTTP (encoded body omitted)");
        } else {
            val contentType = responseHeaders.get("Content-Type");
            val charset = checkNull(contentType, UTF_8::name, CommonReq::parseCharset);

            val responseBody = nullThen(response.body(), Buffer::buffer);
            val contentLength = responseBody.length();
            if (contentLength != 0L) {
                logger.debug("");
                logger.debug(responseBody.toString(charset));
            }
            logger.debug("<-- END HTTP (" + contentLength + "-byte body)");
        }
    }

    private void loggingFailure(HttpContext<Buffer> httpContext, Logger logger) {
        logger.debug("<-- HTTP FAILED: " + httpContext.failure());
    }

    private void logHeader(Logger logger, MultiMap headers, String name) {
        logger.debug(name + ": " + String.join(", ", headers.getAll(name)));
    }

    private boolean bodyHasUnknownEncoding(MultiMap headers) {
        val contentEncoding = headers.get("Content-Encoding");
        if (isNull(contentEncoding)) return false;
        return !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }

    private boolean promisesBody(RequestOptions requestOptions, HttpResponse<?> response) {
        // HEAD requests never yield a body regardless of the response headers.
        if (requestOptions.getMethod() == HttpMethod.HEAD) {
            return false;
        }

        val responseCode = response.statusCode();
        if ((responseCode < CONTINUE.value() || responseCode >= 200) &&
                responseCode != NO_CONTENT.value() &&
                responseCode != NOT_MODIFIED.value()) {
            return true;
        }

        // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
        // response is malformed. For best compatibility, we honor the headers.
        return toLong(response.headers().get("Content-Length"), -1L) != -1L ||
                "chunked".equalsIgnoreCase(response.headers().get("Transfer-Encoding"));
    }
}