package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.httpclient.wfclient.WfFactory.springWfLoader;

public final class WfScannerRegistrar extends SpringScannerRegistrar {

    private final WfFactory.WfLoader wfLoader;

    public WfScannerRegistrar() {
        super(WfScan.class, WfScannerRegistrar.WfClientFactoryBean.class, WfClient.class);
        this.wfLoader = springWfLoader();
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    @Override
    protected void postProcessBeanDefinition(BeanDefinition beanDefinition) {
        super.postProcessBeanDefinition(beanDefinition);
        beanDefinition.getPropertyValues().add("wfLoader", wfLoader);
    }

    public static WfScannerRegistrar.WfClientFactoryBean buildFactoryBean(Class<?> xyzInterface) {
        val factoryBean = new WfScannerRegistrar.WfClientFactoryBean();
        factoryBean.setXyzInterface(xyzInterface);
        factoryBean.setWfLoader(springWfLoader());
        return factoryBean;
    }

    public static class WfClientFactoryBean extends SpringFactoryBean {

        @Setter
        private WfFactory.WfLoader wfLoader;

        @Override
        public Object buildObject(Class<?> xyzInterface) {
            return wfLoader.getClient(xyzInterface);
        }
    }
}
