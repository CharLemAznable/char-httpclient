package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public interface ClientSSLConfigurer extends Configurer {

    default SSLSocketFactory sslSocketFactory() {
        return null;
    }

    default X509TrustManager x509TrustManager() {
        return null;
    }

    default HostnameVerifier hostnameVerifier() {
        return null;
    }
}
