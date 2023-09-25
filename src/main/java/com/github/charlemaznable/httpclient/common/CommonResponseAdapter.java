package com.github.charlemaznable.httpclient.common;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.charset.Charset;

@Getter
@Accessors(fluent = true)
public abstract class CommonResponseAdapter<R/* Response Type */, H/* Response Header Type */, B/* Response Body Type */> {

    final R response;
    final int statusCode;
    final H headers;
    final B body;
    final Charset charset;

    public CommonResponseAdapter(R response, Charset charset) {
        this.response = response;
        this.statusCode = fetchStatusCode(response);
        this.headers = fetchRawResponseHeader(response);
        this.body = fetchRawResponseBody(response);
        this.charset = charset;
    }

    protected abstract int fetchStatusCode(R response);

    protected abstract H fetchRawResponseHeader(R response);

    protected abstract B fetchRawResponseBody(R response);

    public CommonResponse buildCommonResponse() {
        return new CommonResponse(statusCode, buildHttpHeaders(), buildBodyString());
    }

    public abstract HttpHeaders buildHttpHeaders();

    public abstract String buildBodyString();
}
