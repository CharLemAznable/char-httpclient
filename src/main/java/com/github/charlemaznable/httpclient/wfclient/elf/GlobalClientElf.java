package com.github.charlemaznable.httpclient.wfclient.elf;

import com.github.charlemaznable.httpclient.logging.LoggingWfInterceptor;
import com.github.charlemaznable.httpclient.micrometer.TimingWfInterceptor;
import com.github.charlemaznable.httpclient.westcache.WestCacheWfInterceptor;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ServiceLoader;

import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.HAS_WESTCACHE;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class GlobalClientElf {

    private static final WebClient instance;

    static {
        instance = findSupplier().supply();
    }

    public static WebClient globalClient() {
        return instance;
    }

    private static GlobalClientSupplier findSupplier() {
        val suppliers = ServiceLoader.load(GlobalClientSupplier.class).iterator();
        if (!suppliers.hasNext()) return new DefaultGlobalClientSupplier();

        val result = suppliers.next();
        if (suppliers.hasNext())
            throw new IllegalStateException("Multiple GlobalClientSupplier Found");
        return result;
    }

    public static class DefaultGlobalClientSupplier implements GlobalClientSupplier {

        @Override
        public WebClient supply() {
            val builder = WebClient.builder();
            builder.clientConnector(new ReactorClientHttpConnector());
            builder.filter(new LoggingWfInterceptor());
            builder.filter(new TimingWfInterceptor());
            if (HAS_WESTCACHE) builder.filter(new WestCacheWfInterceptor());
            return builder.build();
        }
    }
}
