package com.github.charlemaznable.httpclient.wfclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringErrorConfiguration;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.wfclient.WfScan;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.wfclient.WfFactory.springWfLoader;
import static org.joor.Reflect.on;

@WfScan(basePackageClasses = TestClientScanAnchor.class)
@Import(CommonSpringErrorConfiguration.class)
public class WfSpringErrorConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(springWfLoader()).field("wfCache").call("invalidateAll");
    }
}
