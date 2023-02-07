package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.FixedPathVarsConfigurer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface FixedPathVarsConfig extends FixedPathVarsConfigurer {

    @Config("fixedPathVars")
    String fixedPathVarsString();

    @Override
    default List<Pair<String, String>> fixedPathVars() {
        return notNullThen(fixedPathVarsString(), ConfigurerElf::parseStringToPairList);
    }
}
