package com.github.charlemaznable.httpclient.vxclient.elf;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientBase;
import io.vertx.ext.web.client.impl.WebClientInternal;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.joor.Reflect.on;

@Getter
public final class VxWebClient implements WebClientInternal {

    private final HttpClient httpClient;
    private final WebClientOptions options;
    private final UnaryOperator<WebClientBase> buildOperation;
    private final List<Handler<HttpContext<?>>> interceptors;

    @Delegate(types = WebClient.class)
    private final WebClientBase webClientBase;

    VxWebClient(Vertx vertx, WebClientOptions options) {
        this(vertx.createHttpClient(options), options, UnaryOperator.identity(), null);
    }

    public VxWebClient(HttpClient httpClient, WebClientOptions options,
                       UnaryOperator<WebClientBase> buildOperation,
                       List<Handler<HttpContext<?>>> interceptors) {
        this.httpClient = httpClient;
        this.options = new WebClientOptions(options);
        this.buildOperation = buildOperation;
        this.interceptors = new CopyOnWriteArrayList<>();

        this.webClientBase = this.buildOperation.apply(new WebClientBase(this.httpClient, this.options));
        List<Handler<HttpContext<?>>> newInterceptors = on(this.webClientBase).get("interceptors");
        newArrayList(interceptors).stream().filter(Objects::nonNull)
                .forEach(interceptor -> {
                    if (newInterceptors.parallelStream().noneMatch(i ->
                            i.getClass() == interceptor.getClass())) {
                        this.interceptors.add(interceptor);
                        this.webClientBase.addInterceptor(interceptor);
                    }
                });
    }

    @Override
    public <T> HttpContext<T> createContext(Handler<AsyncResult<HttpResponse<T>>> handler) {
        return this.webClientBase.createContext(handler);
    }

    @Override
    public WebClientInternal addInterceptor(Handler<HttpContext<?>> handler) {
        this.interceptors.add(handler);
        return this.webClientBase.addInterceptor(handler);
    }
}
