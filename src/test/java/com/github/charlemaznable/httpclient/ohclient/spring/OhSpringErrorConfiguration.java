package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringErrorConfiguration;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import com.github.charlemaznable.httpclient.ohclient.OhScan;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.ohclient.OhFactory.springOhLoader;
import static org.joor.Reflect.on;

@OhScan(basePackageClasses = TestClientScanAnchor.class)
@Import(CommonSpringErrorConfiguration.class)
public class OhSpringErrorConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(springOhLoader()).field("ohCache").call("invalidateAll");
    }
}
