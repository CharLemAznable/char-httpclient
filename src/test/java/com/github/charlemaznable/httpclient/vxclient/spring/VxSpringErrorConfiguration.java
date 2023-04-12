package com.github.charlemaznable.httpclient.vxclient.spring;

import com.github.charlemaznable.core.vertx.spring.VertxImport;
import com.github.charlemaznable.httpclient.common.spring.CommonSpringErrorConfiguration;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.vxclient.VxScan;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.vxclient.VxFactory.springVxLoader;
import static org.joor.Reflect.on;

@VertxImport
@VxScan(basePackageClasses = TestClientScanAnchor.class)
@Import(CommonSpringErrorConfiguration.class)
public class VxSpringErrorConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(springVxLoader()).field("vxCache").call("invalidateAll");
    }
}
