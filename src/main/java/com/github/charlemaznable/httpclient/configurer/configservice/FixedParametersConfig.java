package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.FixedParametersConfigurer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface FixedParametersConfig extends FixedParametersConfigurer {

    @Config("fixedParameters")
    String fixedParametersString();

    @Override
    default List<Pair<String, Object>> fixedParameters() {
        return notNullThen(fixedParametersString(), ConfigurerElf::parseStringToPairList);
    }
}
