package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.google.common.net.HttpHeaders;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Properties;
import java.util.function.Predicate;

import static com.github.charlemaznable.core.config.Arguments.argumentsAsProperties;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsProperties;
import static com.github.charlemaznable.core.lang.Propertiess.ssMap;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.httpclient.common.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class CommonConstant {

    public static final Logger log = LoggerFactory.getLogger("char-httpclient");

    public static final ContentFormat.ContentFormatter URL_QUERY_FORMATTER = new ContentFormat.FormContentFormatter();

    public static final String ACCEPT_CHARSET = HttpHeaders.ACCEPT_CHARSET;
    public static final Charset DEFAULT_ACCEPT_CHARSET = UTF_8;
    public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE;
    public static final ContentFormat.ContentFormatter DEFAULT_CONTENT_FORMATTER = new ContentFormat.FormContentFormatter();
    public static final HttpMethod DEFAULT_HTTP_METHOD = GET;

    public static final Predicate<Pair<String, ?>> NOT_BLANK_KEY = p -> isNotBlank(p.getKey());

    static Properties classPathProperties;

    static String substitute(String source) {
        if (isNull(classPathProperties)) {
            classPathProperties = classResourceAsProperties("char-httpclient.env.props");
        }
        return new StringSubstitutor(ssMap(argumentsAsProperties(
                classPathProperties))).replace(source);
    }
}
