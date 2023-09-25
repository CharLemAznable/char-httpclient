package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.httpclient.common.CommonResponseAdapter;
import com.github.charlemaznable.httpclient.common.HttpHeaders;
import lombok.val;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static java.util.Objects.nonNull;
import static org.jooq.lambda.Sneaky.function;

public final class OhResponseAdapter extends CommonResponseAdapter<Response, Headers, ResponseBody> {

    public OhResponseAdapter(Response resonse, Charset charset) {
        super(resonse, charset);
    }

    @Override
    protected int fetchStatusCode(Response response) {
        return response.code();
    }

    @Override
    protected Headers fetchRawResponseHeader(Response response) {
        return response.headers();
    }

    @Override
    protected ResponseBody fetchRawResponseBody(Response response) {
        val responseBody = notNullThen(response.body(), OhResponseBody::new);
        if (nonNull(response.body())) response.close();
        return responseBody;
    }

    @Override
    public HttpHeaders buildHttpHeaders() {
        return new HttpHeaders(headers().toMultimap());
    }

    @Override
    public String buildBodyString() {
        return notNullThen(body(), function(ResponseBody::string));
    }
}
