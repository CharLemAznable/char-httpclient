package com.github.charlemaznable.httpclient.wfclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringConfiguration;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.common.testclient2.TestHttpClientUnscanned;
import com.github.charlemaznable.httpclient.wfclient.WfScan;
import com.github.charlemaznable.httpclient.wfclient.WfScannerRegistrar;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.wfclient.WfFactory.springWfLoader;
import static org.joor.Reflect.on;

@WfScan(basePackageClasses = TestClientScanAnchor.class)
@Import(CommonSpringConfiguration.class)
public class WfSpringConfiguration {

    @Bean
    public WfScannerRegistrar.WfClientFactoryBean testHttpClientUnscanned() {
        return WfScannerRegistrar.buildFactoryBean(TestHttpClientUnscanned.class);
    }

    @PostConstruct
    public void postConstruct() {
        on(springWfLoader()).field("wfCache").call("invalidateAll");
    }
}
