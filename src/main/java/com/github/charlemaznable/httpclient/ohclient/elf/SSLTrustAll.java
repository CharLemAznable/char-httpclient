package com.github.charlemaznable.httpclient.ohclient.elf;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class SSLTrustAll {

    @SneakyThrows
    public static SSLSocketFactory sslSocketFactory() {
        val sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                        // ignored
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                        // ignored
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        }, new SecureRandom());
        return sslContext.getSocketFactory();
    }

    public static X509TrustManager x509TrustManager() {
        TrustManager[] trustManagers = new TrustManager[0];
        try {
            val trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            trustManagers = trustManagerFactory.getTrustManagers();
        } catch (Exception ignored) {
            // ignored
        }
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return DummyX509TrustManager.INSTANCE;
    }

    public static HostnameVerifier hostnameVerifier() {
        return (s, sslSession) -> true;
    }

    @NoArgsConstructor(access = PRIVATE)
    private static class DummyX509TrustManager extends X509ExtendedTrustManager implements X509TrustManager {

        private static final String EXCEPTION_MESSAGE = "No X509TrustManager implementation available";
        private static final X509TrustManager INSTANCE = new DummyX509TrustManager();

        public void checkClientTrusted(X509Certificate[] var1, String var2) throws CertificateException {
            throw new CertificateException(EXCEPTION_MESSAGE);
        }

        public void checkServerTrusted(X509Certificate[] var1, String var2) throws CertificateException {
            throw new CertificateException(EXCEPTION_MESSAGE);
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] var1, String var2, Socket var3) throws CertificateException {
            throw new CertificateException(EXCEPTION_MESSAGE);
        }

        public void checkServerTrusted(X509Certificate[] var1, String var2, Socket var3) throws CertificateException {
            throw new CertificateException(EXCEPTION_MESSAGE);
        }

        public void checkClientTrusted(X509Certificate[] var1, String var2, SSLEngine var3) throws CertificateException {
            throw new CertificateException(EXCEPTION_MESSAGE);
        }

        public void checkServerTrusted(X509Certificate[] var1, String var2, SSLEngine var3) throws CertificateException {
            throw new CertificateException(EXCEPTION_MESSAGE);
        }
    }
}
