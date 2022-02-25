package com.github.charlemaznable.httpclient.ohclient.testclient;

import com.github.charlemaznable.miner.MinerConfig;

@MinerConfig("THREAD_DATA")
public interface TestDefaultContext {

    @MinerConfig(defaultValueProvider = TestDefaultContextProvider.class)
    String thread();
}
