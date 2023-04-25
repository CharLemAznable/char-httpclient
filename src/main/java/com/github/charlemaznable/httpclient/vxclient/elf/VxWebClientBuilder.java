package com.github.charlemaznable.httpclient.vxclient.elf;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.joor.Reflect.on;

@NoArgsConstructor
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public final class VxWebClientBuilder {

    private HttpClient httpClient;
    private WebClientOptions options;
    private UnaryOperator<WebClientBase> buildOperation = UnaryOperator.identity();
    private List<Handler<HttpContext<?>>> interceptors = new CopyOnWriteArrayList<>();

    public VxWebClientBuilder(WebClientBase webClientBase) {
        this.httpClient = getClient(webClientBase);
        this.options = new WebClientOptions(getOptions(webClientBase));
        this.interceptors.addAll(newArrayList(getInterceptors(webClientBase)));
    }

    public VxWebClientBuilder(VxWebClient vxWebClient) {
        this.httpClient = vxWebClient.getHttpClient();
        this.options = new WebClientOptions(vxWebClient.getOptions());
        this.buildOperation = vxWebClient.getBuildOperation();
        this.interceptors.addAll(newArrayList(vxWebClient.getInterceptors()));
    }

    public VxWebClient build() {
        return new VxWebClient(this.httpClient, this.options,
                this.buildOperation, this.interceptors);
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
