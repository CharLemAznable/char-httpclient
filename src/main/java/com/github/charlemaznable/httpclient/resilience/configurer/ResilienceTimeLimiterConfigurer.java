package com.github.charlemaznable.httpclient.resilience.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.resilience.function.ResilienceTimeLimiterRecover;
import io.github.resilience4j.timelimiter.TimeLimiter;

public interface ResilienceTimeLimiterConfigurer extends Configurer {

    TimeLimiter timeLimiter(String defaultName);

    default <T> ResilienceTimeLimiterRecover<T> timeLimiterRecover() {
        return null;
    }
}
