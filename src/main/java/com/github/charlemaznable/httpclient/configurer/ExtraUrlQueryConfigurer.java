package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;

public interface ExtraUrlQueryConfigurer extends Configurer {

    ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder();
}
