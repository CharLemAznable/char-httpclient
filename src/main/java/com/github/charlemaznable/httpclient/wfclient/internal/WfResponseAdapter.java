package com.github.charlemaznable.httpclient.wfclient.internal;

import com.github.charlemaznable.httpclient.common.CommonResponseAdapter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.lang.Condition.nullThen;

public final class WfResponseAdapter extends CommonResponseAdapter<ResponseEntity<byte[]>, HttpHeaders, byte[]> {

    public WfResponseAdapter(ResponseEntity<byte[]> response, Charset charset) {
        super(response, charset);
    }

    @Override
    protected int fetchStatusCode(ResponseEntity<byte[]> response) {
        return response.getStatusCode().value();
    }

    @Override
    protected HttpHeaders fetchRawResponseHeader(ResponseEntity<byte[]> response) {
        return response.getHeaders();
    }

    @Override
    protected byte[] fetchRawResponseBody(ResponseEntity<byte[]> response) {
        return nullThen(response.getBody(), () -> new byte[0]);
    }

    @Override
    public com.github.charlemaznable.httpclient.common.HttpHeaders buildHttpHeaders() {
        return new com.github.charlemaznable.httpclient.common.HttpHeaders(headers());
    }

    @Override
    public String buildBodyString() {
        return string(body(), charset());
    }
}
