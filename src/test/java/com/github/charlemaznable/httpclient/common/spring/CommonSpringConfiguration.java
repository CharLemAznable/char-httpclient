package com.github.charlemaznable.httpclient.common.spring;

import com.github.charlemaznable.configservice.diamond.DiamondScan;
import com.github.charlemaznable.core.spring.ElvesImport;
import com.github.charlemaznable.core.spring.NeoComponentScan;
import com.github.charlemaznable.core.testing.mockito.MockitoSpyProxyEnabled;
import com.github.charlemaznable.httpclient.common.testclient.TestClientScanAnchor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@ElvesImport
@NeoComponentScan(basePackageClasses = TestClientScanAnchor.class)
@DiamondScan(basePackageClasses = TestClientScanAnchor.class)
@MockitoSpyProxyEnabled
public class CommonSpringConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(diamondLoader(springFactory())).field("configCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
    }
}
