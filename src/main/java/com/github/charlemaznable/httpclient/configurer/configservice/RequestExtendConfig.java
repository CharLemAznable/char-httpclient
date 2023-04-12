package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.annotation.RequestExtend;
import com.github.charlemaznable.httpclient.configurer.RequestExtendConfigurer;

public interface RequestExtendConfig extends RequestExtendConfigurer {

    @Config("requestExtender")
    String requestExtenderString();

    @Override
    default RequestExtend.RequestExtender requestExtender() {
        return Objectt.parseObject(requestExtenderString(), RequestExtend.RequestExtender.class);
    }
}
