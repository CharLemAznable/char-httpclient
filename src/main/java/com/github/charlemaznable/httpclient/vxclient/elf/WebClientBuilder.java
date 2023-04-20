package com.github.charlemaznable.httpclient.vxclient.elf;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.joor.Reflect.on;

@NoArgsConstructor
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public final class WebClientBuilder {

    private HttpClient httpClient;
    private WebClientOptions options;
    private List<Handler<HttpContext<?>>> interceptors;

    public WebClientBuilder(WebClientBase webClientBase) {
        this.httpClient = getClient(webClientBase);
        this.options = new WebClientOptions(getOptions(webClientBase));
        this.interceptors = new CopyOnWriteArrayList<>(getInterceptors(webClientBase));
    }

    public WebClient build() {
        val webClientBase = new WebClientBase(httpClient, options);
        on(webClientBase).set("interceptors", new CopyOnWriteArrayList<>());
        for (val interceptor : interceptors) {
            webClientBase.addInterceptor(interceptor);
        }
        return webClientBase;
    }

    private HttpClient getClient(WebClientBase webClientBase) {
        return on(webClientBase).get("client");
    }

    private WebClientOptions getOptions(WebClientBase webClientBase) {
        return on(webClientBase).get("options");
    }

    private List<Handler<HttpContext<?>>> getInterceptors(WebClientBase webClientBase) {
        return on(webClientBase).get("interceptors");
    }
}
