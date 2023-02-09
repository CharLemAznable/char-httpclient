package com.github.charlemaznable.httpclient.ohclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLConfigurer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public interface ClientSSLConfig extends ClientSSLConfigurer {

    @Config("sslSocketFactory")
    String sslSocketFactoryString();

    @Config("x509TrustManager")
    String x509TrustManagerString();

    @Config("hostnameVerifier")
    String hostnameVerifierString();

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
}
