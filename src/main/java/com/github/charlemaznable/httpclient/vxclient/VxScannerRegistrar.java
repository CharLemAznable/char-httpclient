package com.github.charlemaznable.httpclient.vxclient;

import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.httpclient.vxclient.VxFactory.springVxLoader;

public final class VxScannerRegistrar extends SpringScannerRegistrar {

    private final VxFactory.VxLoader vxLoader;

    public VxScannerRegistrar() {
        super(VxScan.class, VxClientFactoryBean.class, VxClient.class);
        this.vxLoader = springVxLoader();
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    @Override
    protected void postProcessBeanDefinition(BeanDefinition beanDefinition) {
        super.postProcessBeanDefinition(beanDefinition);
        beanDefinition.getPropertyValues().add("vxLoader", vxLoader);
    }

    public static VxClientFactoryBean buildFactoryBean(Class<?> xyzInterface) {
        val factoryBean = new VxClientFactoryBean();
        factoryBean.setXyzInterface(xyzInterface);
        factoryBean.setVxLoader(springVxLoader());
        return factoryBean;
    }

    public static class VxClientFactoryBean extends SpringFactoryBean {

        @Setter
        private VxFactory.VxLoader vxLoader;

        @Override
        public Object buildObject(Class<?> xyzInterface) {
            return vxLoader.getClient(xyzInterface);
        }
    }
}
