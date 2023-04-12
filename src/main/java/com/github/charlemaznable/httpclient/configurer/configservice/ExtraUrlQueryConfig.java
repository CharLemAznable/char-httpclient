package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryConfigurer;

public interface ExtraUrlQueryConfig extends ExtraUrlQueryConfigurer {

    @Config("extraUrlQueryBuilder")
    String extraUrlQueryBuilderString();

    @Override
    default ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder() {
        return Objectt.parseObject(extraUrlQueryBuilderString(), ExtraUrlQuery.ExtraUrlQueryBuilder.class);
    }
}
