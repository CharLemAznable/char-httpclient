package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.google.inject.Inject;
import lombok.Getter;

import javax.annotation.Nullable;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.httpclient.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestComponentGuice {

    @Getter
    private final TestHttpClient testHttpClient;

    @Inject
    public TestComponentGuice(@Nullable TestHttpClient testHttpClient) {
        assertTrue(FactoryContext.get() instanceof GuiceFactory);
        this.testHttpClient = nullThen(testHttpClient, () -> getClient(TestHttpClient.class));
    }
}
