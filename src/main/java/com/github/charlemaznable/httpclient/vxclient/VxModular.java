package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.core.guice.CommonModular;
import com.google.inject.Module;
import com.google.inject.Provider;
import lombok.experimental.Delegate;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.httpclient.vxclient.VxFactory.vxLoader;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

public final class VxModular extends CommonModular<VxModular> {

    @Delegate
    private final VxFactory.VxLoader vxLoader;

    public VxModular(Module... modules) {
        this(newArrayList(modules));
    }

    public VxModular(Iterable<? extends Module> modules) {
        super(modules);
        this.vxLoader = vxLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class<?> clazz) {
        return isAnnotated(clazz, VxClient.class);
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getClient(clazz);
    }
}
