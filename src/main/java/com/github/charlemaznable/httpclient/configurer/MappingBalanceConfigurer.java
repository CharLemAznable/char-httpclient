package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.MappingBalance;

public interface MappingBalanceConfigurer extends Configurer {

    MappingBalance.MappingBalancer mappingBalancer();
}
