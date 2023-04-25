package com.github.charlemaznable.httpclient.vxclient.elf;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.impl.WebClientInternal;

public interface VertxScopeClientBuilder {

    WebClientInternal build(Vertx vertx);
}
