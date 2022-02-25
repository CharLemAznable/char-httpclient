package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.miner.MinerModular;
import com.github.charlemaznable.httpclient.common.Mapping.UrlProvider;

import java.lang.reflect.Method;

import static com.github.charlemaznable.miner.MinerFactory.getMiner;
import static com.github.charlemaznable.miner.MinerFactory.springMinerLoader;

public class TestSampleUrlProviderContext implements UrlProvider {

    private TestDefaultContext current;
    private TestDefaultContext spring;
    private TestDefaultContext guice;

    public TestSampleUrlProviderContext() {
        this.current = getMiner(TestDefaultContext.class);
        this.spring = springMinerLoader().getMiner(TestDefaultContext.class);
        this.guice = new MinerModular().getMiner(TestDefaultContext.class);
    }

    @Override
    public String url(Class<?> clazz, Method method) {
        return "/" + current.thread() + "-" + spring.thread() + "-" + guice.thread();
    }
}
