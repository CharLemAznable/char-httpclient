package com.github.charlemaznable.httpclient.resilience.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceBulkheadRecover;
import io.github.resilience4j.bulkhead.Bulkhead;

public interface ResilienceBulkheadConfigurer extends Configurer {

    Bulkhead bulkhead(String defaultName);

    default <T> ResilienceBulkheadRecover<T> bulkheadRecover() {
        return null;
    }
}
