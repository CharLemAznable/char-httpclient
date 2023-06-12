package com.github.charlemaznable.httpclient.wfclient.internal;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.httpclient.common.CommonExecute;
import com.github.charlemaznable.httpclient.wfclient.elf.RequestSpecConfigElf;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.httpclient.common.CommonConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.common.CommonConstant.CONTENT_TYPE;
import static com.github.charlemaznable.httpclient.common.CommonConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonConstant.URL_QUERY_FORMATTER;
import static com.github.charlemaznable.httpclient.common.CommonReq.permitsRequestBody;
import static com.github.charlemaznable.httpclient.wfclient.elf.RequestSpecConfigElf.REQUEST_BODY_AS_STRING;

final class WfExecute extends CommonExecute<WfBase, ResponseEntity<byte[]>, byte[]> {

    public WfExecute(WfMethod wfMethod) {
        super(new WfBase(wfMethod.element().base()), wfMethod);
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

    @Override
    public Object execute() {
        val future = buildRequestSpec()
                .exchangeToMono(this::buildWfResponse)
                .mapNotNull(this::processResponse).toFuture();
        return returnAsyncFromFuture(future);
    }

    private WebClient.RequestBodyUriSpec buildRequestSpec() {
        notNullThenRun(base().requestExtender(), extender -> extender.extend(
                base().headers(), base().pathVars(), base().parameters(), base().contexts()));

        val headersMap = Mapp.<String, String>newHashMap();
        val acceptCharsetName = base().acceptCharset().name();
        headersMap.put(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = base().contentFormatter().contentType();
        headersMap.put(CONTENT_TYPE, contentType);
        for (val header : base().headers()) {
            checkNull(header.getValue(),
                    () -> headersMap.remove(header.getKey()),
                    xx -> headersMap.put(header.getKey(), header.getValue()));
        }

        val executeParams = buildCommonExecuteParams();
        val spec = base().client.method(HttpMethod.valueOf(executeParams.requestMethod()));
        spec.headers(httpHeaders -> httpHeaders.setAll(headersMap));

        if (!permitsRequestBody(executeParams.requestMethod())) {
            val query = URL_QUERY_FORMATTER.format(
                    executeParams.parameterMap(), executeParams.contextMap());
            spec.uri(concatUrlQuery(executeParams.requestUrl(), query));
        } else {
            val content = nullThen(requestBodyRaw(), () ->
                    base().contentFormatter().format(
                            executeParams.parameterMap(), executeParams.contextMap()));
            val contentTypeHeader = nullThen(headersMap.get(CONTENT_TYPE),
                    DEFAULT_CONTENT_FORMATTER::contentType);
            spec.uri(executeParams.requestUrl());
            spec.contentType(MediaType.parseMediaType(contentTypeHeader));
            spec.body(BodyInserters.fromValue(content));
            spec.attribute(REQUEST_BODY_AS_STRING, content);
        }
        RequestSpecConfigElf.configRequestSpec(spec, this);
        return spec;
    }

    private Mono<ResponseEntity<byte[]>> buildWfResponse(ClientResponse clientResponse) {
        return clientResponse.toEntity(byte[].class);
    }

    @Override
    protected int getResponseCode(ResponseEntity<byte[]> response) {
        return response.getStatusCode().value();
    }

    @Override
    protected byte[] getResponseBody(ResponseEntity<byte[]> response) {
        return nullThen(response.getBody(), () -> new byte[0]);
    }

    @Override
    protected String getResponseBodyString(byte[] responseBody) {
        return string(responseBody, base().acceptCharset());
    }

    @Override
    protected Object customProcessReturnTypeValue(int statusCode, byte[] responseBody, Class<?> returnType) {
        if (byte[].class == returnType) {
            return responseBody;
        } else {
            return super.customProcessReturnTypeValue(statusCode, responseBody, returnType);
        }
    }
}
