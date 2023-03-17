package com.github.charlemaznable.httpclient.ohclient.elf;

import lombok.NoArgsConstructor;
import lombok.val;
import okhttp3.Dispatcher;

import java.util.ServiceLoader;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OhDispatcherElf {

    private static final OhDispatcherSupplier instance;

    static {
        instance = findSupplier();
    }

    public static Dispatcher newDispatcher() {
        return instance.supply();
    }

    private static OhDispatcherSupplier findSupplier() {
        val suppliers = ServiceLoader.load(OhDispatcherSupplier.class).iterator();
        if (!suppliers.hasNext()) return new DefaultOhDispatcherSupplier();

        val result = suppliers.next();
        if (suppliers.hasNext())
            throw new IllegalStateException("Multiple OhDispatcherSupplier Found");
        return result;
    }

    private static final class DefaultOhDispatcherSupplier implements OhDispatcherSupplier {

        @Override
        public Dispatcher supply() {
            return new Dispatcher(newCachedThreadPool(r ->
                    new Thread(r, "OhClient Dispatcher")));
        }
    }
}
