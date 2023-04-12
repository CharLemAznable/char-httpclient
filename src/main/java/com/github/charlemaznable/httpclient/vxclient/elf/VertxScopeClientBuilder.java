package com.github.charlemaznable.httpclient.vxclient.elf;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

public interface VertxScopeClientBuilder {

    WebClient build(Vertx vertx);
}
