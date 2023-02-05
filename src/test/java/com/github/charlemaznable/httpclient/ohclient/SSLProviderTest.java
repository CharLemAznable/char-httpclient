package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientSSL;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLDisabledConfigurer;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.X509Certificate;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.joor.Reflect.on;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnusedReturnValue")
public class SSLProviderTest {

    private static final String FAILED = "Failed to connect to /127.0.0.1:41124";
    private static final OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @Test
    public void testSSLDef() {
        val httpClient = ohLoader.getClient(SSLDefHttpClient.class);
        val callback = onClass(httpClient.getClass()).field("BUDDY$DELEGATE_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        assertTrue(okHttpClient.sslSocketFactory() instanceof TestSSLSocketFactory);
        assertTrue(okHttpClient.hostnameVerifier() instanceof TestHostnameVerifier);
    }

    @Test
    public void testSSLAll() {
        val httpClient = ohLoader.getClient(SSLAllHttpClient.class);
        val callback = onClass(httpClient.getClass()).field("BUDDY$DELEGATE_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        assertTrue(okHttpClient.sslSocketFactory() instanceof TestSSLSocketFactory);
        assertTrue(okHttpClient.hostnameVerifier() instanceof TestHostnameVerifier);
    }

    @SneakyThrows
    @Test
    public void testSSLDefParam() {
        val httpClient = ohLoader.getClient(SSLDefParamHttpClient.class);
        val sslSocketFactory = new TestSSLSocketFactory();
        val hostnameVerifier = new TestHostnameVerifier();
        try {
            httpClient.sample(sslSocketFactory, hostnameVerifier);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41122", e.getMessage());
        }
        try {
            httpClient.sample(null, null);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41122", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testSSLAllParam() {
        val httpClient = ohLoader.getClient(SSLAllParamHttpClient.class);
        val sslSocketFactory = new TestSSLSocketFactory();
        val x509TrustManager = new TestX509TrustManager();
        val hostnameVerifier = new TestHostnameVerifier();
        try {
            httpClient.sample(sslSocketFactory, x509TrustManager, hostnameVerifier);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41123", e.getMessage());
        }
        try {
            httpClient.sample(null, null, null);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41123", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testMethodSSL() {
        val httpClient = ohLoader.getClient(MethodSSLHttpClient.class);
        try {
            httpClient.sample();
        } catch (Exception e) {
            assertEquals(FAILED, e.getMessage());
        }
        try {
            httpClient.sampleDef();
        } catch (Exception e) {
            assertEquals(FAILED, e.getMessage());
        }
        try {
            httpClient.sampleAll();
        } catch (Exception e) {
            assertEquals(FAILED, e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testDisabledSSL() {
        val httpClient = ohLoader.getClient(DisableSSLHttpClient.class);
        try {
            httpClient.sample();
        } catch (Exception e) {
            assertEquals(FAILED, e.getMessage());
        }
        try {
            httpClient.sampleDisabled();
        } catch (Exception e) {
            assertEquals(FAILED, e.getMessage());
        }

        val httpClientNeo = ohLoader.getClient(DisableSSLHttpClientNeo.class);
        try {
            httpClientNeo.sample();
        } catch (Exception e) {
            assertEquals(FAILED, e.getMessage());
        }
        try {
            httpClientNeo.sampleDisabled();
        } catch (Exception e) {
            assertEquals(FAILED, e.getMessage());
        }
    }

    @OhClient
    @Mapping("${root}:41120")
    @ClientSSL(
            sslSocketFactory = TestSSLSocketFactory.class,
            hostnameVerifier = TestHostnameVerifier.class)
    public interface SSLDefHttpClient {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41121")
    @ClientSSL(
            sslSocketFactory = TestSSLSocketFactory.class,
            x509TrustManager = TestX509TrustManager.class,
            hostnameVerifier = TestHostnameVerifier.class)
    public interface SSLAllHttpClient {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41122")
    @ClientSSL(
            sslSocketFactory = TestSSLSocketFactory.class,
            hostnameVerifier = TestHostnameVerifier.class)
    public interface SSLDefParamHttpClient {

        String sample(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier);
    }

    @OhClient
    @Mapping("${root}:41123")
    @ClientSSL(
            sslSocketFactory = TestSSLSocketFactory.class,
            x509TrustManager = TestX509TrustManager.class,
            hostnameVerifier = TestHostnameVerifier.class)
    public interface SSLAllParamHttpClient {

        String sample(SSLSocketFactory sslSocketFactory, X509TrustManager x509TrustManager, HostnameVerifier hostnameVerifier);
    }

    @OhClient
    @Mapping("${root}:41124")
    public interface MethodSSLHttpClient {

        String sample();

        @ClientSSL(
                sslSocketFactory = TestSSLSocketFactory.class,
                hostnameVerifier = TestHostnameVerifier.class)
        String sampleDef();

        @ClientSSL(
                sslSocketFactory = TestSSLSocketFactory.class,
                x509TrustManager = TestX509TrustManager.class,
                hostnameVerifier = TestHostnameVerifier.class)
        String sampleAll();
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ClientSSL.Disabled
    public @interface Disabled {}

    @OhClient
    @Mapping("${root}:41124")
    @ClientSSL(
            sslSocketFactory = TestSSLSocketFactory.class,
            hostnameVerifier = TestHostnameVerifier.class)
    public interface DisableSSLHttpClient {

        String sample();

        @Disabled
        String sampleDisabled();
    }

    @OhClient
    @Mapping("${root}:41124")
    @ConfigureWith(DisableSSLHttpClientConfig.class)
    public interface DisableSSLHttpClientNeo {

        @ConfigureWith(DefaultConfig.class)
        String sample();

        @ConfigureWith(SampleConfig.class)
        String sampleDisabled();
    }

    public static class DisableSSLHttpClientConfig implements ClientSSLConfigurer {

        @Override
        public SSLSocketFactory sslSocketFactory() {
            return new TestSSLSocketFactory();
        }

        @Override
        public X509TrustManager x509TrustManager() {
            return new TestX509TrustManager();
        }

        @Override
        public HostnameVerifier hostnameVerifier() {
            return new TestHostnameVerifier();
        }
    }

    public static class DefaultConfig implements ClientSSLConfigurer {}

    public static class SampleConfig implements ClientSSLDisabledConfigurer {}

    public static class TestSSLSocketFactory extends SSLSocketFactory {

        @Override
        public String[] getDefaultCipherSuites() {
            return new String[0];
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return new String[0];
        }

        @Override
        public Socket createSocket(Socket socket, String s, int i, boolean b) {
            return null;
        }

        @Override
        public Socket createSocket(String s, int i) {
            return null;
        }

        @Override
        public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) {
            return null;
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i) {
            return null;
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) {
            return null;
        }
    }

    public static class TestX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            // empty
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            // empty
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static class TestHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
