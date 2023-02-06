package com.github.charlemaznable.httpclient.configurer;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.core.lang.Objectt;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.github.charlemaznable.httpclient.common.RequestExtend;
import com.github.charlemaznable.httpclient.common.ResponseParse;
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

public interface CommonAbstractConfig extends MappingConfigurer,
        AcceptCharsetConfigurer, ContentFormatConfigurer, RequestMethodConfigurer,
        FixedHeadersConfigurer, FixedPathVarsConfigurer, FixedParametersConfigurer, FixedContextsConfigurer,
        StatusFallbacksConfigurer, StatusSeriesFallbacksConfigurer,
        RequestExtendConfigurer, ResponseParseConfigurer, ExtraUrlQueryConfigurer, MappingBalanceConfigurer {

    @Config("urls")
    String urlsString();

    @Config("acceptCharset")
    String acceptCharsetString();

    @Config("contentFormatter")
    String contentFormatterString();

    @Config("requestMethod")
    String requestMethodString();

    @Config("fixedHeaders")
    String fixedHeadersString();

    @Config("fixedPathVars")
    String fixedPathVarsString();

    @Config("fixedParameters")
    String fixedParametersString();

    @Config("fixedContexts")
    String fixedContextsString();

    @Config("statusFallbackMapping")
    String statusFallbackMappingString();

    @Config("statusSeriesFallbackMapping")
    String statusSeriesFallbackMappingString();

    @Config("requestExtender")
    String requestExtenderString();

    @Config("responseParser")
    String responseParserString();

    @Config("extraUrlQueryBuilder")
    String extraUrlQueryBuilderString();

    @Config("mappingBalancer")
    String mappingBalancerString();

    @Override
    default List<String> urls() {
        return notNullThen(urlsString(), v -> Splitter.on(",")
                .omitEmptyStrings().trimResults().splitToList(v));
    }

    @Override
    default Charset acceptCharset() {
        try {
            return notNullThen(acceptCharsetString(), Charset::forName);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    default ContentFormat.ContentFormatter contentFormatter() {
        return notNullThen(contentFormatterString(), v -> Optional
                .ofNullable(ContentFormat.ContentType.resolve(v))
                .map(ContentFormat.ContentType::getContentFormatter).orElse(null));
    }

    @Override
    default HttpMethod requestMethod() {
        try {
            return notNullThen(requestMethodString(), HttpMethod::valueOf);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    default List<Pair<String, String>> fixedHeaders() {
        return notNullThen(fixedHeadersString(), ConfigurerElf::parseStringToPairList);
    }

    @Override
    default List<Pair<String, String>> fixedPathVars() {
        return notNullThen(fixedPathVarsString(), ConfigurerElf::parseStringToPairList);
    }

    @Override
    default List<Pair<String, Object>> fixedParameters() {
        return notNullThen(fixedParametersString(), ConfigurerElf::parseStringToPairList);
    }

    @Override
    default List<Pair<String, Object>> fixedContexts() {
        return notNullThen(fixedContextsString(), ConfigurerElf::parseStringToPairList);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping() {
        return notNullThen(statusFallbackMappingString(), v -> {
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

    @Override
    default RequestExtend.RequestExtender requestExtender() {
        return Objectt.parseObject(requestExtenderString(), RequestExtend.RequestExtender.class);
    }

    @Override
    default ResponseParse.ResponseParser responseParser() {
        return Objectt.parseObject(responseParserString(), ResponseParse.ResponseParser.class);
    }

    @Override
    default ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder() {
        return Objectt.parseObject(extraUrlQueryBuilderString(), ExtraUrlQuery.ExtraUrlQueryBuilder.class);
    }

    @Override
    default MappingBalance.MappingBalancer mappingBalancer() {
        return notNullThen(mappingBalancerString(), v -> Optional
                .ofNullable(MappingBalance.BalanceType.resolve(v))
                .map(MappingBalance.BalanceType::getMappingBalancer).orElse(null));
    }
}
