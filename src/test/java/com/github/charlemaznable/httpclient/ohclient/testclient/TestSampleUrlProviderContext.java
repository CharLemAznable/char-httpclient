package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.configservice.diamond.DiamondModular;
import com.github.charlemaznable.httpclient.common.Mapping.UrlProvider;

import java.lang.reflect.Method;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.configservice.diamond.DiamondFactory.getDiamond;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public class TestSampleUrlProviderContext implements UrlProvider {

    private TestDefaultContext current;
    private TestDefaultContext spring;
    private TestDefaultContext guice;

    public TestSampleUrlProviderContext() {
        this.current = getDiamond(TestDefaultContext.class);
        this.spring = diamondLoader(springFactory()).getDiamond(TestDefaultContext.class);
        this.guice = new DiamondModular().getDiamond(TestDefaultContext.class);
    }

    @Override
    public String url(Class<?> clazz, Method method) {
        return "/" + current.thread() + "-" + spring.thread() + "-" + guice.thread();
    }
}
