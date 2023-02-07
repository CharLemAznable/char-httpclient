package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.configurer.StatusSeriesFallbacksConfigurer;
import com.google.common.base.Splitter;
import lombok.val;

import java.util.Map;

import static com.github.charlemaznable.core.lang.Clz.isAssignable;
import static com.github.charlemaznable.core.lang.ClzPath.findClass;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.intOf;
import static java.util.Objects.nonNull;

public interface StatusSeriesFallbacksConfig extends StatusSeriesFallbacksConfigurer {

    @Config("statusSeriesFallbackMapping")
    String statusSeriesFallbackMappingString();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default Map<HttpStatus.Series, Class<? extends FallbackFunction>> statusSeriesFallbackMapping() {
        return notNullThen(statusSeriesFallbackMappingString(), v -> {
            val mapping = Splitter.on("&").omitEmptyStrings()
                    .trimResults().withKeyValueSeparator("=").split(v);
            return mapping.entrySet().stream()
                    .filter(e -> nonNull(HttpStatus.Series.resolve(intOf(e.getKey()))))
                    .filter(e -> isAssignable(findClass(e.getValue()), FallbackFunction.class))
                    .collect(toMap(e -> HttpStatus.Series.valueOf(intOf(e.getKey())),
                            e -> (Class<? extends FallbackFunction>) findClass(e.getValue())));
        });
    }
}
