package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.core.mutiny.MutinyBuildHelper;
import com.github.charlemaznable.core.reactor.ReactorBuildHelper;
import com.github.charlemaznable.core.rxjava.RxJava1BuildHelper;
import com.github.charlemaznable.core.rxjava.RxJava2BuildHelper;
import com.github.charlemaznable.core.rxjava.RxJava3BuildHelper;
import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.vxclient.elf.HttpContextConfigElf;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.WebClientInternal;
import io.vertx.ext.web.codec.impl.BodyCodecImpl;
import lombok.val;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.CommonConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonConstant.URL_QUERY_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonReq.parseCharset;
import static com.github.charlemaznable.httpclient.common.CommonReq.permitsRequestBody;

final class VxExecute extends CommonExecute<VxBase, HttpResponse<Buffer>, Buffer> {

    public VxExecute(VxMethod vxMethod) {
        super(new VxBase(vxMethod.element().base()), vxMethod);
    }

    @Override
    protected boolean processParameterType(Object argument, Class<?> parameterType) {
        if (argument instanceof WebClientInternal client) {
            base().client = client;
            return true;
        } else {
            return super.processParameterType(argument, parameterType);
        }
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Object execute() {
        val request = buildRequest();
        val promise = Promise.<HttpResponse<Buffer>>promise();
        val context = base().client.createContext(promise);
        HttpContextConfigElf.configHttpContext(context, this);
        context.set(VxExecuteRequest.class.getName(), request);
        context.prepareRequest(request.bufferHttpRequest, null, request.buffer);
        val future = promise.future().map(this::processResponse);

        if (executeMethod().returnJavaFuture()) {
            return future.toCompletionStage().toCompletableFuture();
        } else if (executeMethod().returnReactorMono()) {
            return ReactorBuildHelper.buildMonoFromVertxFuture(future);
        } else if (executeMethod().returnRxJavaSingle()) {
            return RxJava1BuildHelper.buildSingleFromVertxFuture(future);
        } else if (executeMethod().returnRxJava2Single()) {
            return RxJava2BuildHelper.buildSingleFromVertxFuture(future);
        } else if (executeMethod().returnRxJava3Single()) {
            return RxJava3BuildHelper.buildSingleFromVertxFuture(future);
        } else if (executeMethod().returnMutinyUni()) {
            return MutinyBuildHelper.buildUniFromVertxFuture(future);
        } else {
            return future;
        }
    }

    private VxExecuteRequest buildRequest() {
        notNullThenRun(base().requestExtender(), extender -> extender.extend(
                base().headers(), base().pathVars(), base().parameters(), base().contexts()));

        val httpHeaders = HeadersMultiMap.httpHeaders();
        val acceptCharsetName = base().acceptCharset().name();
        httpHeaders.set(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = base().contentFormatter().contentType();
        httpHeaders.set(CONTENT_TYPE, contentType);
        for (val header : base().headers()) {
            checkNull(header.getValue(),
                    () -> httpHeaders.remove(header.getKey()),
                    xx -> httpHeaders.set(header.getKey(), header.getValue()));
        }

        val executeParams = buildCommonExecuteParams();
        val request = new VxExecuteRequest();
        if (!permitsRequestBody(executeParams.requestMethod())) {
            val query = URL_QUERY_FORMATTER.format(
                    executeParams.parameterMap(), executeParams.contextMap());
            request.requestUrl = concatUrlQuery(executeParams.requestUrl(), query);
            request.bufferHttpRequest = base().client.requestAbs(
                    HttpMethod.valueOf(executeParams.requestMethod()), request.requestUrl);
        } else {
            val content = nullThen(requestBodyRaw(), () ->
                    base().contentFormatter().format(
                            executeParams.parameterMap(), executeParams.contextMap()));
            val contentTypeHeader = nullThen(httpHeaders.get(CONTENT_TYPE),
                    DEFAULT_CONTENT_FORMATTER::contentType);
            val charset = parseCharset(contentTypeHeader);
            request.requestUrl = executeParams.requestUrl();
            request.bufferHttpRequest = base().client.requestAbs(
                    HttpMethod.valueOf(executeParams.requestMethod()), request.requestUrl);
            request.buffer = Buffer.buffer(content, charset);
        }
        request.bufferHttpRequest.putHeaders(httpHeaders);

        return request;
    }

    @Override
    protected int getResponseCode(HttpResponse<Buffer> response) {
        return response.statusCode();
    }

    @Override
    protected Buffer getResponseBody(HttpResponse<Buffer> response) {
        return nullThen(response.body(), Buffer::buffer);
    }

    @Override
    protected String getResponseBodyString(Buffer responseBody) {
        return responseBody.toString(base().acceptCharset());
    }

    @Override
    protected Object customProcessReturnTypeValue(int statusCode, Buffer responseBody, Class<?> returnType) {
        if (Buffer.class.isAssignableFrom(returnType)) {
            return responseBody;
        } else if (byte[].class == returnType) {
            return notNullThen(responseBody, Buffer::getBytes);
        } else if (JsonObject.class.isAssignableFrom(returnType)) {
            return notNullThen(responseBody, BodyCodecImpl.JSON_OBJECT_DECODER);
        } else if (JsonArray.class.isAssignableFrom(returnType)) {
            return notNullThen(responseBody, BodyCodecImpl.JSON_ARRAY_DECODER);
        } else {
            return super.customProcessReturnTypeValue(statusCode, responseBody, returnType);
        }
    }
}
