package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.configurer.CommonAbstractConfig;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;

public interface OkHttpAbstractConfig extends CommonAbstractConfig,
        ClientProxyConfigurer, ClientSSLConfigurer, IsolatedConnectionPoolConfigurer,
        ClientTimeoutConfigurer, ClientInterceptorsConfigurer, ClientLoggingLevelConfigurer {

    @Config("proxy")
    String proxyString();

    @Config("sslSocketFactory")
    String sslSocketFactoryString();

    @Config("x509TrustManager")
    String x509TrustManagerString();

    @Config("hostnameVerifier")
    String hostnameVerifierString();

    @Config("isolatedConnectionPool")
    String isolatedConnectionPoolString();

    @Config("callTimeout")
    String callTimeoutString();

    @Config("connectTimeout")
    String connectTimeoutString();

    @Config("readTimeout")
    String readTimeoutString();

    @Config("writeTimeout")
    String writeTimeoutString();

    @Config("interceptors")
    String interceptorsString();

    @Config("loggingLevel")
    String loggingLevelString();

    @Override
    default Proxy proxy() {
        return notNullThen(proxyString(), v -> {
            val uri = URI.create(v);
            return new Proxy(Proxy.Type.valueOf(uri.getScheme().toUpperCase()),
                    new InetSocketAddress(uri.getHost(), uri.getPort()));
        });
    }

    @Override
    default SSLSocketFactory sslSocketFactory() {
        return Objectt.parseObject(sslSocketFactoryString(), SSLSocketFactory.class);
    }

    @Override
    default X509TrustManager x509TrustManager() {
        return Objectt.parseObject(x509TrustManagerString(), X509TrustManager.class);
    }

    @Override
    default HostnameVerifier hostnameVerifier() {
        return Objectt.parseObject(hostnameVerifierString(), HostnameVerifier.class);
    }

    @Override
    default boolean isolatedConnectionPool() {
        return BooleanUtils.toBoolean(isolatedConnectionPoolString());
    }

    @Override
    default long callTimeout() {
        return NumberUtils.toLong(callTimeoutString(), DEFAULT_CALL_TIMEOUT);
    }

    @Override
    default long connectTimeout() {
        return NumberUtils.toLong(connectTimeoutString(), DEFAULT_CONNECT_TIMEOUT);
    }

    @Override
    default long readTimeout() {
        return NumberUtils.toLong(readTimeoutString(), DEFAULT_READ_TIMEOUT);
    }

    @Override
    default long writeTimeout() {
        return NumberUtils.toLong(writeTimeoutString(), DEFAULT_WRITE_TIMEOUT);
    }

    @Override
    default List<Interceptor> interceptors() {
        return Objectt.parseObjects(interceptorsString(), Interceptor.class);
    }

    @Override
    default HttpLoggingInterceptor.Level loggingLevel() {
        try {
            return notNullThen(loggingLevelString(), HttpLoggingInterceptor.Level::valueOf);
        } catch (Exception e) {
            return null;
        }
    }
}
