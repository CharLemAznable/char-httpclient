package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.ohclient.elf.RequestBuilderConfigElf;
import lombok.Lombok;
import lombok.val;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSource;

import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.CommonConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonConstant.URL_QUERY_FORMATTER;
import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static java.util.Objects.requireNonNullElse;
import static org.jooq.lambda.Sneaky.function;

final class OhExecute extends CommonExecute<OhBase, OhMethod, Response, OhResponseAdapter> {

    public OhExecute(OhMethod ohMethod) {
        super(new OhBase(ohMethod.element().base()), ohMethod);
    }

    @Override
    protected boolean processParameterType(Object argument, Class<?> parameterType) {
        if (argument instanceof OkHttpClient client) {
            base().client = client;
            return true;
        } else {
            return super.processParameterType(argument, parameterType);
        }
    }

    @Override
    public Object execute() {
        val completableFuture = decorateAsyncExecute(() -> {
            val future = new OhCallbackFuture<>(this::processResponse);
            base().client.newCall(buildRequest()).enqueue(future);
            return future;
        });
        if (executeMethod().returnFuture()) {
            return adaptationFromFuture(completableFuture);
        }
        return getFromFuture(completableFuture);
    }

    private Request buildRequest() {
        val requestBuilder = new Request.Builder();

        notNullThenRun(base().requestExtender(), extender -> extender.extend(
                base().headers(), base().pathVars(), base().parameters(), base().contexts()));

        val headersBuilder = new Headers.Builder();
        val acceptCharsetName = base().acceptCharset().name();
        headersBuilder.set(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = base().contentFormatter().contentType();
        headersBuilder.set(CONTENT_TYPE, contentType);
        for (val header : base().headers()) {
            checkNull(header.getValue(),
                    () -> headersBuilder.removeAll(header.getKey()),
                    xx -> headersBuilder.set(header.getKey(), header.getValue()));
        }
        requestBuilder.headers(headersBuilder.build());

        val executeParams = buildCommonExecuteParams();
        if (!HttpMethod.permitsRequestBody(executeParams.requestMethod())) {
            requestBuilder.method(executeParams.requestMethod(), null);
            val query = URL_QUERY_FORMATTER.format(
                    executeParams.parameterMap(), executeParams.contextMap());
            requestBuilder.url(concatUrlQuery(executeParams.requestUrl(), query));
        } else {
            val content = nullThen(requestBodyRaw(), () ->
                    base().contentFormatter().format(
                            executeParams.parameterMap(), executeParams.contextMap()));
            val contentTypeHeader = nullThen(headersBuilder.get(CONTENT_TYPE),
                    DEFAULT_CONTENT_FORMATTER::contentType);
            requestBuilder.method(executeParams.requestMethod(), RequestBody.create(
                    content, MediaType.parse(contentTypeHeader)));
            requestBuilder.url(executeParams.requestUrl());
        }
        RequestBuilderConfigElf.configRequestBuilder(requestBuilder, this);
        return requestBuilder.build();
    }

    @Override
    protected OhResponseAdapter responseAdapter(Response response) {
        return new OhResponseAdapter(response, base().acceptCharset());
    }

    @Override
    protected Object customProcessReturnTypeValue(OhResponseAdapter responseAdapter, Class<?> returnType) {
        if (ResponseBody.class == returnType) {
            return responseAdapter.body();
        } else if (InputStream.class == returnType) {
            return notNullThen(responseAdapter.body(), ResponseBody::byteStream);
        } else if (BufferedSource.class == returnType) {
            return (notNullThen(responseAdapter.body(), ResponseBody::source));
        } else if (byte[].class == returnType) {
            return notNullThen(responseAdapter.body(), function(ResponseBody::bytes));
        } else if (Reader.class == returnType) {
            return notNullThen(responseAdapter.body(), ResponseBody::charStream);
        } else {
            return super.customProcessReturnTypeValue(responseAdapter, returnType);
        }
    }

    private Object getFromFuture(Future<Object> future) {
        try {
            return getUninterruptibly(future);
        } catch (ExecutionException e) {
            throw Lombok.sneakyThrow(requireNonNullElse(e.getCause(), e));
        }
    }
}
