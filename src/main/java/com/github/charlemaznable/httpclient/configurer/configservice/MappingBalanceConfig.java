package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.github.charlemaznable.httpclient.configurer.MappingBalanceConfigurer;

import java.util.Optional;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface MappingBalanceConfig extends MappingBalanceConfigurer {

    @Config("mappingBalancer")
    String mappingBalancerString();

    @Override
    default MappingBalance.MappingBalancer mappingBalancer() {
        return notNullThen(mappingBalancerString(), v -> Optional
                .ofNullable(MappingBalance.BalanceType.resolve(v))
                .map(MappingBalance.BalanceType::getMappingBalancer).orElse(null));
    }
}
