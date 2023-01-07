package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.httpclient.ohclient.OhFactory.springOhLoader;

public final class OhScannerRegistrar extends SpringScannerRegistrar {

    private static final OhLoader springOhLoader = springOhLoader();

    public OhScannerRegistrar() {
        super(OhScan.class, OhClientFactoryBean.class, OhClient.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class OhClientFactoryBean extends SpringFactoryBean {

        @Override
        public Object buildObject(Class<?> xyzInterface) {
            return springOhLoader.getClient(xyzInterface);
        }
    }
}
