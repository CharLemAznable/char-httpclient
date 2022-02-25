package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.github.charlemaznable.miner.MinerConfig.DefaultValueProvider;
import com.github.charlemaznable.core.spring.SpringFactory;

import java.lang.reflect.Method;

public class TestDefaultContextProvider implements DefaultValueProvider {

    private String init;

    public TestDefaultContextProvider() {
        if (FactoryContext.get() instanceof SpringFactory) {
            init = "Spring";
        } else if (FactoryContext.get() instanceof GuiceFactory) {
            init = "Guice";
        } else init = "";
    }

    @Override
    public String defaultValue(Class<?> minerClass, Method method) {
        if (FactoryContext.get() instanceof SpringFactory) {
            return init + "Spring";
        } else if (FactoryContext.get() instanceof GuiceFactory) {
            return init + "Guice";
        } else return init;
    }
}
