package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.RequestExtend;

public interface RequestExtendConfigurer extends Configurer {

    RequestExtend.RequestExtender requestExtender();
}
