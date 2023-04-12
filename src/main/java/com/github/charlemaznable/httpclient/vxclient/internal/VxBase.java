package com.github.charlemaznable.httpclient.vxclient.internal;

import com.github.charlemaznable.httpclient.common.CommonBase;
import com.github.charlemaznable.httpclient.vxclient.elf.VertxScopeClientElf;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
final class VxBase extends CommonBase<VxBase> {

    static VxBase defaultBase(Vertx vertx) {
        val defaultBase = new VxBase(vertx);
        defaultBase.client = VertxScopeClientElf.scopeClient(vertx);
        return defaultBase;
    }

    final Vertx vertx;
    WebClient client;

    public VxBase(VxBase other) {
        super(other);
        this.vertx = other.vertx;
        this.client = other.client;
    }
}
