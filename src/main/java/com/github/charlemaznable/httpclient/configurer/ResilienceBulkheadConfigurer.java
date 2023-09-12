package com.github.charlemaznable.httpclient.configurer;

import io.github.resilience4j.bulkhead.Bulkhead;

public interface ResilienceBulkheadConfigurer extends Configurer {

    Bulkhead bulkhead();
}
