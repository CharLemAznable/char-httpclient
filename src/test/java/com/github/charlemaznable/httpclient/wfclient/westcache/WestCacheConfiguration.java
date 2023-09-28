package com.github.charlemaznable.httpclient.wfclient.westcache;

import com.github.bingoohuang.westcache.spring.WestCacheableEnabled;
import com.github.bingoohuang.westcache.spring.WestCacheableScan;
import com.github.charlemaznable.httpclient.wfclient.WfScan;
import com.github.charlemaznable.httpclient.wfclient.configurer.WebFluxClientBuilderConfigurer;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.Executors;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static com.github.charlemaznable.httpclient.configurer.InitializationContext.getInitializingClass;
import static com.github.charlemaznable.httpclient.wfclient.WfFactory.springWfLoader;
import static org.joor.Reflect.on;

@Configuration
@WestCacheableEnabled
@WestCacheableScan
@WfScan
public class WestCacheConfiguration {

    private final LoggingMeterRegistry meterRegistry;

    public WestCacheConfiguration() {
        val meterLogger = LoggerFactory.getLogger(this.getClass().getPackageName() + ".meter");
        this.meterRegistry = new LoggingMeterRegistry(new LoggingRegistryConfig() {
            @Override
            public String get(@NotNull String key) {
                return null;
            }

            @Override
            public @NotNull Duration step() {
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
    public LoggingMetricsClientConfigurer loggingMetricsClientConfigurer() {
        return new LoggingMetricsClientConfigurer(this.meterRegistry);
    }

    @AllArgsConstructor
    public static class LoggingMetricsClientConfigurer implements WebFluxClientBuilderConfigurer {

        private MeterRegistry meterRegistry;

        @Override
        public WebClient.Builder configBuilder(WebClient.Builder builder) {
            return builder.filters(filters -> filters.add(0, new LoggingMetricsFilter(
                    getInitializingClass().getSimpleName(), meterRegistry)));
        }
    }

    @AllArgsConstructor
    public static class LoggingMetricsFilter implements ExchangeFilterFunction {

        private String name;
        private MeterRegistry meterRegistry;

        @Nonnull
        @Override
        public Mono<ClientResponse> filter(@Nonnull ClientRequest request, @Nonnull ExchangeFunction next) {
            return next.exchange(request).name(name).tap(Micrometer.metrics(meterRegistry));
        }
    }
}
