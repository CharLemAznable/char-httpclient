package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import com.github.charlemaznable.httpclient.ohclient.OhFactory.OhLoader;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.httpclient.ohclient.OhFactory.springOhLoader;

public final class OhScannerRegistrar extends SpringScannerRegistrar {

    private final OhLoader ohLoader;

    public OhScannerRegistrar() {
        super(OhScan.class, OhClientFactoryBean.class, OhClient.class);
        this.ohLoader = springOhLoader();
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    @Override
    protected void postProcessBeanDefinition(BeanDefinition beanDefinition) {
        super.postProcessBeanDefinition(beanDefinition);
        beanDefinition.getPropertyValues().add("ohLoader", ohLoader);
    }

    public static class OhClientFactoryBean extends SpringFactoryBean {

        @Setter
        private OhLoader ohLoader;

        @Override
        public Object buildObject(Class<?> xyzInterface) {
            return ohLoader.getClient(xyzInterface);
        }
    }
}
