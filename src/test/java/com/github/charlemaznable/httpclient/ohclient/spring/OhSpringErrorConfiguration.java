package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.core.spring.ElvesImport;
import com.github.charlemaznable.core.spring.NeoComponentScan;
import com.github.charlemaznable.httpclient.ohclient.OhScan;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestClientScanAnchor;
import org.n3r.diamond.client.impl.MockDiamondServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static com.github.charlemaznable.httpclient.ohclient.OhFactory.springOhLoader;
import static org.joor.Reflect.on;

@ElvesImport
@NeoComponentScan(basePackageClasses = TestClientScanAnchor.class)
@OhScan(basePackageClasses = TestClientScanAnchor.class)
public class OhSpringErrorConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(diamondLoader(springFactory())).field("configCache").call("invalidateAll");
        on(springOhLoader()).field("ohCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
    }
}
