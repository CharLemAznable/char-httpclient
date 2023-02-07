package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.FixedContextsConfigurer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface FixedContextsConfig extends FixedContextsConfigurer {

    @Config("fixedContexts")
    String fixedContextsString();

    @Override
    default List<Pair<String, Object>> fixedContexts() {
        return notNullThen(fixedContextsString(), ConfigurerElf::parseStringToPairList);
    }
}
