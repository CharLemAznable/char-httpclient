package com.github.charlemaznable.httpclient.wfclient.westcache;

import com.github.bingoohuang.westcache.spring.WestCacheableEnabled;
import com.github.bingoohuang.westcache.spring.WestCacheableScan;
import com.github.charlemaznable.httpclient.micrometer.AutoBindMeterRegistryEnabled;
import com.github.charlemaznable.httpclient.wfclient.WfScan;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.val;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.Executors;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static com.github.charlemaznable.httpclient.wfclient.WfFactory.springWfLoader;
import static org.joor.Reflect.on;

@Configuration
@WestCacheableEnabled
@WestCacheableScan
@WfScan
@AutoBindMeterRegistryEnabled
public class WestCacheConfiguration {

    private final LoggingMeterRegistry meterRegistry;

    public WestCacheConfiguration() {
        val meterLogger = LoggerFactory.getLogger(this.getClass().getPackageName() + ".meter");
        this.meterRegistry = new LoggingMeterRegistry(new LoggingRegistryConfig() {
            @Override
            public String get(@Nonnull String key) {
                return null;
            }

            @Override
            public @Nonnull Duration step() {
                return Duration.ofSeconds(1);
            }
        }, Clock.SYSTEM, meterLogger::debug);
    }

    @PostConstruct
    public void postConstruct() {
        this.meterRegistry.start(Executors.defaultThreadFactory());
        on(diamondLoader(springFactory())).field("configCache").call("invalidateAll");
        on(springWfLoader()).field("wfCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
        this.meterRegistry.stop();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        val creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return this.meterRegistry;
    }
}
