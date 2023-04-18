package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.vxclient.elf.HttpContextConfigElf;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.impl.WebClientInternal;
import io.vertx.ext.web.codec.impl.BodyCodecImpl;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import static com.github.charlemaznable.core.codec.Json.spec;
import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.codec.Json.unJsonArray;
import static com.github.charlemaznable.core.codec.Xml.unXml;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.CommonConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonConstant.URL_QUERY_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonReq.parseCharset;
import static com.github.charlemaznable.httpclient.common.CommonReq.permitsRequestBody;
import static java.util.Objects.nonNull;

final class VxExecute extends CommonExecute<VxBase, HttpResponse<Buffer>, Buffer> {

    public VxExecute(VxMethod vxMethod) {
        super(new VxBase(vxMethod.element().base()), vxMethod);
    }

    @Override
    protected boolean processParameterType(Object argument, Class<?> parameterType) {
        if (argument instanceof WebClient client) {
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
        val client = (WebClientInternal) base().client;

        val promise = Promise.<HttpResponse<Buffer>>promise();
        val context = client.createContext(promise);
        HttpContextConfigElf.configHttpContext(context, this, request);
        context.prepareRequest(request.bufferHttpRequest, null, request.buffer);
        val responseFuture = promise.future().map(this::processResponse);

        val vxMethod = (VxMethod) this.executeMethod();
        if (vxMethod.returnRxJavaSingle) {
            return VxRxHelper.buildRxSingle(responseFuture);
        } else if (vxMethod.returnRxJava2Single) {
            return VxRxHelper.buildRxSingle2(responseFuture);
        } else if (vxMethod.returnRxJava3Single) {
            return VxRxHelper.buildRxSingle3(responseFuture);
        } else {
            return responseFuture;
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
            return notNullThen(responseBody, buffer -> returnObject(buffer, returnType));
        }
    }

    private Object returnObject(Buffer responseBody, Class<?> returnType) {
        val content = responseBody.toString(base().acceptCharset());
        if (isBlank(content)) return null;
        if (nonNull(base().responseParser())) {
            val contextMap = base().contexts().stream().collect(toMap(Pair::getKey, Pair::getValue));
            return base().responseParser().parse(content, returnType, contextMap);
        }
        if (content.startsWith("<")) return spec(unXml(content), returnType);
        if (content.startsWith("[")) return unJsonArray(content, returnType);
        if (content.startsWith("{")) return unJson(content, returnType);
        throw new IllegalArgumentException("Parse response body Error: \n" + content);
    }
}
