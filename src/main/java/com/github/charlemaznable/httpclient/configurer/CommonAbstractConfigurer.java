package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.google.common.base.Splitter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.charlemaznable.core.lang.Clz.isAssignable;
import static com.github.charlemaznable.core.lang.ClzPath.findClass;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.intOf;
import static java.util.Objects.nonNull;

public interface CommonAbstractConfigurer extends ConfigGetter, MappingConfigurer,
        AcceptCharsetConfigurer, ContentFormatConfigurer, RequestMethodConfigurer,
        FixedHeadersConfigurer, FixedPathVarsConfigurer, FixedParametersConfigurer, FixedContextsConfigurer,
        StatusFallbacksConfigurer, StatusSeriesFallbacksConfigurer,
        RequestExtendConfigurer, ResponseParseConfigurer, ExtraUrlQueryConfigurer, MappingBalanceConfigurer {

    @Override
    default Charset acceptCharset() {
        try {
            return notNullThen(getString("acceptCharset"), Charset::forName);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    default ContentFormat.ContentFormatter contentFormatter() {
        return notNullThen(getString("contentType"), v -> Optional
                .ofNullable(ContentFormat.ContentType.resolve(v))
                .map(ContentFormat.ContentType::getContentFormatter).orElse(null));
    }

    @Override
    default HttpMethod requestMethod() {
        try {
            return notNullThen(getString("requestMethod"), HttpMethod::valueOf);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    default List<Pair<String, String>> fixedHeaders() {
        return notNullThen(getString("headers"), ConfigurerElf::parseStringToPairList);
    }

    @Override
    default List<Pair<String, String>> fixedPathVars() {
        return notNullThen(getString("pathVars"), ConfigurerElf::parseStringToPairList);
    }

    @Override
    default List<Pair<String, Object>> fixedParameters() {
        return notNullThen(getString("parameters"), ConfigurerElf::parseStringToPairList);
    }

    @Override
    default List<Pair<String, Object>> fixedContexts() {
        return notNullThen(getString("contexts"), ConfigurerElf::parseStringToPairList);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping() {
        return notNullThen(getString("statusFallbacks"), v -> {
            val mapping = Splitter.on("&").omitEmptyStrings()
                    .trimResults().withKeyValueSeparator("=").split(v);
            return mapping.entrySet().stream()
                    .filter(e -> nonNull(HttpStatus.resolve(intOf(e.getKey()))))
                    .filter(e -> isAssignable(findClass(e.getValue()), FallbackFunction.class))
                    .collect(toMap(e -> HttpStatus.valueOf(intOf(e.getKey())),
                            e -> (Class<? extends FallbackFunction>) findClass(e.getValue())));
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default Map<HttpStatus.Series, Class<? extends FallbackFunction>> statusSeriesFallbackMapping() {
        return notNullThen(getString("statusSeriesFallbacks"), v -> {
            val mapping = Splitter.on("&").omitEmptyStrings()
                    .trimResults().withKeyValueSeparator("=").split(v);
            return mapping.entrySet().stream()
                    .filter(e -> nonNull(HttpStatus.Series.resolve(intOf(e.getKey()))))
                    .filter(e -> isAssignable(findClass(e.getValue()), FallbackFunction.class))
                    .collect(toMap(e -> HttpStatus.Series.valueOf(intOf(e.getKey())),
                            e -> (Class<? extends FallbackFunction>) findClass(e.getValue())));
        });
    }

    @Override
    default MappingBalance.MappingBalancer mappingBalancer() {
        return notNullThen(getString("mappingBalancer"), v -> Optional
                .ofNullable(MappingBalance.BalanceType.resolve(v))
                .map(MappingBalance.BalanceType::getMappingBalancer).orElse(null));
    }
}
