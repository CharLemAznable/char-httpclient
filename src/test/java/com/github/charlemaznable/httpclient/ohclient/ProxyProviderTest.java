package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientProxy;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyDisabledConfigurer;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("UnusedReturnValue")
public class ProxyProviderTest {

    private static final String LOCAL_HOST = "127.0.0.1";
    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @Test
    public void testProxyPlain() {
        val httpClient = ohLoader.getClient(ProxyPlainHttpClient.class);
        val callback = on(httpClient).field("CGLIB$CALLBACK_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        val address = (InetSocketAddress) checkNotNull(okHttpClient.proxy()).address();
        assertEquals(LOCAL_HOST, address.getAddress().getHostAddress());
        assertEquals(41111, address.getPort());
    }

    @SneakyThrows
    @Test
    public void testProxyParam() {
        val httpClient = ohLoader.getClient(ProxyParamHttpClient.class);
        val proxyParam = new Proxy(Type.HTTP, new InetSocketAddress(LOCAL_HOST, 41115));
        try {
            httpClient.sample(proxyParam);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41115", e.getMessage());
        }
        try {
            httpClient.sample(null);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41114", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testMethodProxy() {
        val httpClient = ohLoader.getClient(MethodProxyHttpClient.class);
        try {
            httpClient.sampleDefault();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41117", e.getMessage());
        }
        try {
            httpClient.samplePlain();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41118", e.getMessage());
        }
        try {
            httpClient.sampleDisabled();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41116", e.getMessage());
        }

        val httpClientNeo = ohLoader.getClient(MethodProxyHttpClientNeo.class);
        try {
            httpClientNeo.sampleDefault();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41117", e.getMessage());
        }
        try {
            httpClientNeo.samplePlain();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41118", e.getMessage());
        }
        try {
            httpClientNeo.sampleDisabled();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41116", e.getMessage());
        }
    }

    @OhClient
    @Mapping("${root}:41110")
    @ClientProxy(host = "127.0.0.1", port = 41111)
    public interface ProxyPlainHttpClient {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41114")
    @ClientProxy(host = "127.0.0.1", port = 41115)
    public interface ProxyParamHttpClient {

        String sample(Proxy proxy);
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ClientProxy.Disabled
    public @interface Disabled {}

    @OhClient
    @Mapping("${root}:41116")
    @ClientProxy(host = "127.0.0.1", port = 41117)
    public interface MethodProxyHttpClient {

        String sampleDefault();

        @ClientProxy(host = "127.0.0.1", port = 41118)
        String samplePlain();

        @Disabled
        String sampleDisabled();
    }

    @OhClient
    @Mapping("${root}:41116")
    @ConfigureWith(MethodProxyHttpClientConfig.class)
    public interface MethodProxyHttpClientNeo {

        String sampleDefault();

        @ConfigureWith(SampleConfig.class)
        String samplePlain();

        @ConfigureWith(SampleDisabledConfig.class)
        String sampleDisabled();
    }

    public static class MethodProxyHttpClientConfig implements ClientProxyConfigurer {

        @Override
        public Proxy proxy() {
            return new Proxy(Type.HTTP, new InetSocketAddress(LOCAL_HOST, 41117));
        }
    }

    public static class SampleConfig implements ClientProxyConfigurer {

        @Override
        public Proxy proxy() {
            return new Proxy(Type.HTTP, new InetSocketAddress(LOCAL_HOST, 41118));
        }
    }

    public static class SampleDisabledConfig implements ClientProxyDisabledConfigurer {}
}
