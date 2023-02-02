package com.github.charlemaznable.httpclient.configurer;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface FixedParametersConfigurer extends Configurer {

    List<Pair<String, Object>> fixedParameters();
}
