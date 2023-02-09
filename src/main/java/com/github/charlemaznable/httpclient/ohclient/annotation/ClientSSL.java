package com.github.charlemaznable.httpclient.ohclient.annotation;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientSSL {

    Class<? extends SSLSocketFactory> sslSocketFactory()
            default SSLSocketFactory.class;

    Class<? extends X509TrustManager> x509TrustManager()
            default X509TrustManager.class;

    Class<? extends HostnameVerifier> hostnameVerifier()
            default HostnameVerifier.class;

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface DisabledSSLSocketFactory {
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface DisabledX509TrustManager {
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface DisabledHostnameVerifier {
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @DisabledSSLSocketFactory
    @DisabledX509TrustManager
    @DisabledHostnameVerifier
    @interface Disabled {
    }
}
