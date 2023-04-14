package com.github.charlemaznable.httpclient.ohclient.elf;

import com.github.charlemaznable.httpclient.logging.LoggingOhInterceptor;
import com.github.charlemaznable.httpclient.westcache.WestCacheOhInterceptor;
import lombok.NoArgsConstructor;
import lombok.val;
import okhttp3.OkHttpClient;

import java.util.ServiceLoader;

import static com.github.charlemaznable.httpclient.westcache.WestCacheConstant.HAS_WESTCACHE;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class GlobalClientElf {

    private static final OkHttpClient instance;

    static {
        instance = findSupplier().supply();
    }

    public static OkHttpClient globalClient() {
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

    private static final class DefaultGlobalClientSupplier implements GlobalClientSupplier {

        @Override
        public OkHttpClient supply() {
            val builder = new OkHttpClient.Builder();
            builder.addInterceptor(new LoggingOhInterceptor());
            if (HAS_WESTCACHE) builder.addInterceptor(new WestCacheOhInterceptor());
            return builder.build();
        }
    }
}
