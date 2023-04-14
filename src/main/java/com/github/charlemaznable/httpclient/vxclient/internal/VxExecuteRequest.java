package com.github.charlemaznable.httpclient.vxclient.internal;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import lombok.Getter;

@Getter
public final class VxExecuteRequest {

    String requestUrl;
    HttpRequest<Buffer> bufferHttpRequest;
    Buffer buffer;
}
