package com.github.charlemaznable.httpclient.ohclient.elf;

import lombok.val;

import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

public final class OhExecutorServiceBuilderElf {

    private static final OhExecutorServiceBuilder builder;

    static {
        builder = findBuilder();
    }

    public static ExecutorService buildExecutorService() {
        return builder.buildExecutorService();
    }

    private static OhExecutorServiceBuilder findBuilder() {
        val builders = ServiceLoader.load(OhExecutorServiceBuilder.class).iterator();
        if (!builders.hasNext()) return new DefaultOhExecutorServiceBuilder();

        val result = builders.next();
        if (builders.hasNext())
            throw new IllegalStateException("Multiple OhExecutorServiceBuilder Defined");
        return result;
    }

    static final class DefaultOhExecutorServiceBuilder implements OhExecutorServiceBuilder {

        @Override
        public ExecutorService buildExecutorService() {
            return newCachedThreadPool();
        }
    }
}
