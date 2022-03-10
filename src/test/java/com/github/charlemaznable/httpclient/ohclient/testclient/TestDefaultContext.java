package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@DiamondConfig("THREAD_DATA")
public interface TestDefaultContext {

    @DiamondConfig(defaultValueProvider = TestDefaultContextProvider.class)
    String thread();
}
