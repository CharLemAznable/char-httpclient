package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.guice.CommonModular;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import com.google.inject.Module;
import com.google.inject.Provider;
import lombok.experimental.Delegate;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.ohclient.OhFactory.ohLoader;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

public final class OhModular extends CommonModular<OhModular> {

    @Delegate
    private final OhLoader ohLoader;

    public OhModular(Module... modules) {
        this(newArrayList(modules));
    }

    public OhModular(Iterable<? extends Module> modules) {
        super(modules);
        this.ohLoader = ohLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class<?> clazz) {
        return isAnnotated(clazz, OhClient.class);
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getClient(clazz);
    }
}
