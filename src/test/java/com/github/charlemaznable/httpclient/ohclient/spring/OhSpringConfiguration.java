package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringConfiguration;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.common.testclient2.TestHttpClientUnscanned;
import com.github.charlemaznable.httpclient.ohclient.OhScan;
import com.github.charlemaznable.httpclient.ohclient.OhScannerRegistrar;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.ohclient.OhFactory.springOhLoader;
import static org.joor.Reflect.on;

@OhScan(basePackageClasses = TestClientScanAnchor.class)
@Import(CommonSpringConfiguration.class)
public class OhSpringConfiguration {

    @Bean
    public OhScannerRegistrar.OhClientFactoryBean testHttpClientUnscanned() {
        return OhScannerRegistrar.buildFactoryBean(TestHttpClientUnscanned.class);
    }

    @PostConstruct
    public void postConstruct() {
        on(springOhLoader()).field("ohCache").call("invalidateAll");
    }
}
