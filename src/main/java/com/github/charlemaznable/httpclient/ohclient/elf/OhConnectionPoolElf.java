package com.github.charlemaznable.httpclient.ohclient.elf;

import lombok.NoArgsConstructor;
import lombok.val;
import okhttp3.ConnectionPool;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OhConnectionPoolElf {

    private static final OhConnectionPoolSupplier instance;

    static {
        instance = findSupplier();
    }

    public static ConnectionPool newConnectionPool() {
        return instance.supply();
    }

    private static OhConnectionPoolSupplier findSupplier() {
        val suppliers = ServiceLoader.load(OhConnectionPoolSupplier.class).iterator();
        if (!suppliers.hasNext()) return new DefaultOhConnectionPoolSupplier();

        val result = suppliers.next();
        if (suppliers.hasNext())
            throw new IllegalStateException("Multiple OhConnectionPoolSupplier Found");
        return result;
    }

    private static final class DefaultOhConnectionPoolSupplier implements OhConnectionPoolSupplier {

        @Override
        public ConnectionPool supply() {
            return new ConnectionPool();
        }
    }
}
