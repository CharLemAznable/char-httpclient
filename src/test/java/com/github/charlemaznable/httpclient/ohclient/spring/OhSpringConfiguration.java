package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.miner.MinerScan;
import com.github.charlemaznable.httpclient.ohclient.OhScan;
import com.github.charlemaznable.httpclient.ohclient.testclient.TestClientScanAnchor;
import com.github.charlemaznable.core.spring.NeoComponentScan;
import com.github.charlemaznable.core.spring.ElvesImport;
import org.n3r.diamond.client.impl.MockDiamondServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.github.charlemaznable.miner.MinerFactory.springMinerLoader;
import static com.github.charlemaznable.httpclient.ohclient.OhFactory.springOhLoader;
import static org.joor.Reflect.on;

@ElvesImport
@NeoComponentScan(basePackageClasses = TestClientScanAnchor.class)
@MinerScan(basePackageClasses = TestClientScanAnchor.class)
@OhScan(basePackageClasses = TestClientScanAnchor.class)
public class OhSpringConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(springMinerLoader()).field("minerCache").call("invalidateAll");
        on(springOhLoader()).field("ohCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
    }
}
