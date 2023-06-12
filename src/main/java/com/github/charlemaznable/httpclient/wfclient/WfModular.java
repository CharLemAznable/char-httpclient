package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.core.guice.CommonModular;
import com.google.inject.Module;
import com.google.inject.Provider;
import lombok.experimental.Delegate;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.wfclient.WfFactory.wfLoader;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

public final class WfModular extends CommonModular<WfModular> {

    @Delegate
    private final WfFactory.WfLoader wfLoader;

    public WfModular(Module... modules) {
        this(newArrayList(modules));
    }

    public WfModular(Iterable<? extends Module> modules) {
        super(modules);
        this.wfLoader = wfLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class<?> clazz) {
        return isAnnotated(clazz, WfClient.class);
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getClient(clazz);
    }
}
