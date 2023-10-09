package com.github.charlemaznable.httpclient.micrometer;

import com.github.charlemaznable.httpclient.common.MeterBinder;
import com.github.charlemaznable.httpclient.ohclient.internal.OhDummy;
import com.github.charlemaznable.httpclient.vxclient.internal.VxDummy;
import com.github.charlemaznable.httpclient.wfclient.internal.WfDummy;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.annotation.Nonnull;

public class AutoBindMeterRegistryBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor {

    private MeterRegistry meterRegistry;

    @Override
    public void setBeanFactory(@Nonnull BeanFactory beanFactory) throws BeansException {
        try {
            this.meterRegistry = beanFactory.getBean(MeterRegistry.class);
        } catch (Exception e) {
            // ignored
        }
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (isClientInstance(bean) && bean instanceof MeterBinder meterBinder) {
            meterBinder.bindTo(this.meterRegistry);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    private boolean isClientInstance(@Nonnull Object bean) {
        return bean instanceof OhDummy || bean instanceof VxDummy || bean instanceof WfDummy;
    }
}
