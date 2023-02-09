package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.FixedHeadersConfigurer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface FixedHeadersConfig extends FixedHeadersConfigurer {

    @Config("fixedHeaders")
    String fixedHeadersString();

    @Override
    default List<Pair<String, String>> fixedHeaders() {
        return notNullThen(fixedHeadersString(), ConfigurerElf::parseStringToPairList);
    }
}
