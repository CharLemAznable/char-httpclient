package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.httpclient.common.CommonResponseAdapter;
import com.github.charlemaznable.httpclient.common.HttpHeaders;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

public final class VxResponseAdapter extends CommonResponseAdapter<HttpResponse<Buffer>, MultiMap, Buffer> {

    public VxResponseAdapter(HttpResponse<Buffer> response, Charset charset) {
        super(response, charset);
    }

    @Override
    protected int fetchStatusCode(HttpResponse<Buffer> response) {
        return response.statusCode();
    }

    @Override
    protected MultiMap fetchRawResponseHeader(HttpResponse<Buffer> response) {
        return response.headers();
    }

    @Override
    protected Buffer fetchRawResponseBody(HttpResponse<Buffer> response) {
        return nullThen(response.body(), Buffer::buffer);
    }

    @Override
    public HttpHeaders buildHttpHeaders() {
        return new HttpHeaders(headers());
    }

    @Override
    public String buildBodyString() {
        return body().toString(charset());
    }
}
