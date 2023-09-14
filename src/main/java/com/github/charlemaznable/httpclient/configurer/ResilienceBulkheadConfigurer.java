package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.httpclient.common.ResilienceBulkheadRecover;
import io.github.resilience4j.bulkhead.Bulkhead;

public interface ResilienceBulkheadConfigurer extends Configurer {

    Bulkhead bulkhead(String defaultName);

    default ResilienceBulkheadRecover<?> bulkheadRecover() {
        return null;
    }
}
