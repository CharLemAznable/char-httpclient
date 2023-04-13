package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.westcache.WestCacheKey;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
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
import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.HAS_WESTCACHE;
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

    private static final class Request {
        private HttpRequest<Buffer> bufferHttpRequest;
        private Buffer buffer;
    }

    @Override
    public Object execute() {
        val request = buildRequest();
        val promise = Promise.<HttpResponse<Buffer>>promise();
        val client = (WebClientInternal) base().client;
        val context = client.createContext(promise);

        // westcache supported
        if (HAS_WESTCACHE) {
            val method = executeMethod().method();
            val option = WestCacheOption.parseWestCacheable(method);
            if (nonNull(option)) {
                context.set(WestCacheOption.class.getName(), option);
                val cacheKey = option.getKeyer().getCacheKey(option,
                        method, executeMethod().defaultClass(), args());
                context.set(WestCacheKey.class.getName(), new WestCacheKey(cacheKey));
            }
        }

        context.prepareRequest(request.bufferHttpRequest, null, request.buffer);
        return promise.future().map(this::processResponse);
    }

    private Request buildRequest() {
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
        val request = new Request();
        if (!permitsRequestBody(executeParams.requestMethod())) {
            val query = URL_QUERY_FORMATTER.format(
                    executeParams.parameterMap(), executeParams.contextMap());
            val url = concatUrlQuery(executeParams.requestUrl(), query);
            request.bufferHttpRequest = base().client.requestAbs(
                    HttpMethod.valueOf(executeParams.requestMethod()), url);
        } else {
            val content = nullThen(requestBodyRaw(), () ->
                    base().contentFormatter().format(
                            executeParams.parameterMap(), executeParams.contextMap()));
            val contentTypeHeader = nullThen(httpHeaders.get(CONTENT_TYPE),
                    DEFAULT_CONTENT_FORMATTER::contentType);
            val charset = parseCharset(contentTypeHeader);
            request.bufferHttpRequest = base().client.requestAbs(
                    HttpMethod.valueOf(executeParams.requestMethod()), executeParams.requestUrl());
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
