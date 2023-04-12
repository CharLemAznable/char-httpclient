package com.github.charlemaznable.httpclient.vxclient.spring;

import com.github.charlemaznable.core.vertx.spring.VertxImport;
import com.github.charlemaznable.httpclient.common.spring.CommonSpringConfiguration;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.common.testclient2.TestHttpClientUnscanned;
import com.github.charlemaznable.httpclient.vxclient.VxScan;
import com.github.charlemaznable.httpclient.vxclient.VxScannerRegistrar;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.vxclient.VxFactory.springVxLoader;
import static org.joor.Reflect.on;

@VxScan(basePackageClasses = TestClientScanAnchor.class)
@Import(CommonSpringConfiguration.class)
@VertxImport
public class VxSpringConfiguration {

    @Bean
    public VxScannerRegistrar.VxClientFactoryBean testHttpClientUnscanned() {
        return VxScannerRegistrar.buildFactoryBean(TestHttpClientUnscanned.class);
    }

    @PostConstruct
    public void postConstruct() {
        on(springVxLoader()).field("vxCache").call("invalidateAll");
    }
}
