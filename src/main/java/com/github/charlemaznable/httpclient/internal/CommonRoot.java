package com.github.charlemaznable.httpclient.internal;

import com.github.charlemaznable.configservice.ConfigFactory;
import com.github.charlemaznable.configservice.ConfigListenerRegister;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reflectt;
import com.github.charlemaznable.core.lang.Str;
import com.github.charlemaznable.httpclient.common.AcceptCharset;
import com.github.charlemaznable.httpclient.common.Bundle;
import com.github.charlemaznable.httpclient.common.CncRequest;
import com.github.charlemaznable.httpclient.common.CncResponse;
import com.github.charlemaznable.httpclient.common.ConfigureWith;
import com.github.charlemaznable.httpclient.common.ContentFormat;
import com.github.charlemaznable.httpclient.common.Context;
import com.github.charlemaznable.httpclient.common.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.FixedContext;
import com.github.charlemaznable.httpclient.common.FixedHeader;
import com.github.charlemaznable.httpclient.common.FixedParameter;
import com.github.charlemaznable.httpclient.common.FixedPathVar;
import com.github.charlemaznable.httpclient.common.Header;
import com.github.charlemaznable.httpclient.common.HttpMethod;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.MappingBalance;
import com.github.charlemaznable.httpclient.common.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.common.Parameter;
import com.github.charlemaznable.httpclient.common.PathVar;
import com.github.charlemaznable.httpclient.common.RequestBodyRaw;
import com.github.charlemaznable.httpclient.common.RequestExtend;
import com.github.charlemaznable.httpclient.common.RequestMethod;
import com.github.charlemaznable.httpclient.common.ResponseParse;
import com.github.charlemaznable.httpclient.common.StatusErrorThrower;
import com.github.charlemaznable.httpclient.common.StatusFallback;
import com.github.charlemaznable.httpclient.common.StatusSeriesFallback;
import com.github.charlemaznable.httpclient.configurer.AcceptCharsetConfigurer;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.configurer.ContentFormatConfigurer;
import com.github.charlemaznable.httpclient.configurer.DefaultFallbackDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryConfigurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedContextsConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedHeadersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedParametersConfigurer;
import com.github.charlemaznable.httpclient.configurer.FixedPathVarsConfigurer;
import com.github.charlemaznable.httpclient.configurer.InitializationConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingBalanceConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingConfigurer;
import com.github.charlemaznable.httpclient.configurer.MappingMethodNameDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestMethodConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusFallbacksConfigurer;
import com.github.charlemaznable.httpclient.configurer.StatusSeriesFallbacksConfigurer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.codec.Json.desc;
import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.DEFAULT_HTTP_METHOD;
import static com.github.charlemaznable.httpclient.internal.CommonConstant.NOT_BLANK_KEY;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.log;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.joor.Reflect.on;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

@SuppressWarnings("rawtypes")
@Getter
@Accessors(fluent = true)
public class CommonRoot {

    protected static final ContentFormat.ContentFormatter URL_QUERY_FORMATTER = new ContentFormat.FormContentFormatter();

    protected Charset acceptCharset;
    protected ContentFormat.ContentFormatter contentFormatter;
    protected HttpMethod httpMethod;
    protected List<Pair<String, String>> headers;
    protected List<Pair<String, String>> pathVars;
    protected List<Pair<String, Object>> parameters;
    protected List<Pair<String, Object>> contexts;

    protected Map<HttpStatus, Class<? extends FallbackFunction>> statusFallbackMapping;
    protected Map<HttpStatus.Series, Class<? extends FallbackFunction>> statusSeriesFallbackMapping;

    protected RequestExtend.RequestExtender requestExtender;
    protected ResponseParse.ResponseParser responseParser;

    protected ExtraUrlQuery.ExtraUrlQueryBuilder extraUrlQueryBuilder;

    protected MappingBalance.MappingBalancer mappingBalancer;

    protected static Configurer checkConfigurer(AnnotatedElement element, Factory factory) {
        val configureWith = getMergedAnnotation(element, ConfigureWith.class);
        if (isNull(configureWith)) return null;
        val configurerClass = configureWith.value();
        val configurer = FactoryContext.build(factory, configurerClass);
        if (nonNull(configurer)) return configurer;
        try {
            return ConfigFactory.configLoader(factory).getConfig(configurerClass);
        } catch (Exception e) {
            log.warn("Load Configurer by ConfigService with exception: ", e);
            return null;
        }
    }

    protected static void checkConfigurerIsRegisterThenRun(Configurer configurer,
                                                           Consumer<ConfigListenerRegister> consumer) {
        if (configurer instanceof ConfigListenerRegister register) {
            notNullThenRun(consumer, c -> c.accept(register));
        }
    }

    protected static void setUpBeforeInitialization(Configurer configurer, Class clazz) {
        if (configurer instanceof InitializationConfigurer initializationConfigurer) {
            initializationConfigurer.setUpBeforeInitialization(clazz, null);
        } else {
            InitializationConfigurer.INSTANCE.setUpBeforeInitialization(clazz, null);
        }
    }

    protected static void tearDownAfterInitialization(Configurer configurer, Class clazz) {
        if (configurer instanceof InitializationConfigurer initializationConfigurer) {
            initializationConfigurer.tearDownAfterInitialization(clazz, null);
        } else {
            InitializationConfigurer.INSTANCE.tearDownAfterInitialization(clazz, null);
        }
    }

    protected static void setUpBeforeInitialization(Configurer configurer, Method method, Class clazz,
                                                    Configurer superConfigurer) {
        if (configurer instanceof InitializationConfigurer initializationConfigurer) {
            initializationConfigurer.setUpBeforeInitialization(clazz, method);
        } else if (superConfigurer instanceof InitializationConfigurer initializationConfigurer) {
            initializationConfigurer.setUpBeforeInitialization(clazz, method);
        } else {
            InitializationConfigurer.INSTANCE.setUpBeforeInitialization(clazz, method);
        }
    }

    protected static void tearDownAfterInitialization(Configurer configurer, Method method, Class clazz,
                                                      Configurer superConfigurer) {
        if (configurer instanceof InitializationConfigurer initializationConfigurer) {
            initializationConfigurer.tearDownAfterInitialization(clazz, method);
        } else if (superConfigurer instanceof InitializationConfigurer initializationConfigurer) {
            initializationConfigurer.tearDownAfterInitialization(clazz, method);
        } else {
            InitializationConfigurer.INSTANCE.tearDownAfterInitialization(clazz, method);
        }
    }

    protected static List<String> checkMappingUrls(Configurer configurer, AnnotatedElement element,
                                                   Function<String, String> substitutor) {
        if (configurer instanceof MappingConfigurer mappingConfigurer)
            return newArrayList(mappingConfigurer.urls())
                    .stream().map(substitutor).toList();
        val mapping = getMergedAnnotation(element, Mapping.class);
        return notNullThen(mapping, anno -> Arrays
                .stream(anno.value()).map(substitutor).toList());
    }

    protected static boolean checkMappingMethodNameDisabled(Configurer configurer, Class clazz) {
        if (configurer instanceof MappingMethodNameDisabledConfigurer disabledConfigurer)
            return disabledConfigurer.disabledMappingMethodName();
        return isAnnotated(clazz, MappingMethodNameDisabled.class);
    }

    protected static List<String> checkRequestUrls(Configurer configurer, Method method,
                                                   Function<String, String> substitutor,
                                                   List<String> baseUrls, boolean mappingMethodNameDisabled) {
        val urls = emptyThen(checkMappingUrls(configurer, method, substitutor), () ->
                newArrayList(mappingMethodNameDisabled ? "" : "/" + method.getName()));
        Set<String> requestUrls = newHashSet();
        for (val url : urls) {
            if (isBlank(url)) {
                requestUrls.addAll(baseUrls);
            } else {
                requestUrls.addAll(baseUrls.stream()
                        .map(base -> checkBlank(base, () -> url, b -> b + url)).toList());
            }
        }
        return requestUrls.stream().distinct().collect(Collectors.toList());
    }

    protected static void initialize(CommonRoot root, Factory factory,
                                     Configurer configurer, Class clazz) {
        root.acceptCharset = nullThen(checkAcceptCharset(
                configurer, clazz), () -> DEFAULT_ACCEPT_CHARSET);
        root.contentFormatter = nullThen(checkContentFormatter(
                configurer, clazz, factory), () -> DEFAULT_CONTENT_FORMATTER);
        root.httpMethod = nullThen(checkHttpMethod(
                configurer, clazz), () -> DEFAULT_HTTP_METHOD);
        root.headers = checkFixedHeaders(configurer, clazz);
        root.pathVars = checkFixedPathVars(configurer, clazz);
        root.parameters = checkFixedParameters(configurer, clazz);
        root.contexts = checkFixedContexts(configurer, clazz);

        root.statusFallbackMapping = checkStatusFallbackMapping(configurer, clazz);
        root.statusSeriesFallbackMapping = defaultFallback(configurer, clazz);
        root.statusSeriesFallbackMapping.putAll(checkStatusSeriesFallbackMapping(configurer, clazz));

        root.requestExtender = checkRequestExtender(configurer, clazz, factory);
        root.responseParser = checkResponseParser(configurer, clazz, factory);
        root.extraUrlQueryBuilder = checkExtraUrlQueryBuilder(configurer, clazz, factory);
        root.mappingBalancer = nullThen(checkMappingBalancer(
                configurer, clazz, factory), MappingBalance.RandomBalancer::new);
    }

    protected static void initialize(CommonRoot root, Factory factory,
                                     Configurer configurer, Method method, CommonRoot superRoot) {
        root.acceptCharset = nullThen(checkAcceptCharset(
                configurer, method), superRoot::acceptCharset);
        root.contentFormatter = nullThen(checkContentFormatter(
                configurer, method, factory), superRoot::contentFormatter);
        root.httpMethod = nullThen(checkHttpMethod(
                configurer, method), superRoot::httpMethod);
        root.headers = newArrayList(superRoot.headers());
        root.headers.addAll(checkFixedHeaders(configurer, method));
        root.pathVars = newArrayList(superRoot.pathVars());
        root.pathVars.addAll(checkFixedPathVars(configurer, method));
        root.parameters = newArrayList(superRoot.parameters());
        root.parameters.addAll(checkFixedParameters(configurer, method));
        root.contexts = newArrayList(superRoot.contexts());
        root.contexts.addAll(checkFixedContexts(configurer, method));

        root.statusFallbackMapping = newHashMap(superRoot.statusFallbackMapping());
        root.statusFallbackMapping.putAll(checkStatusFallbackMapping(configurer, method));
        root.statusSeriesFallbackMapping = newHashMap(superRoot.statusSeriesFallbackMapping());
        root.statusSeriesFallbackMapping.putAll(checkStatusSeriesFallbackMapping(configurer, method));

        root.requestExtender = checkRequestExtender(configurer, method, factory, superRoot);
        root.responseParser = checkResponseParser(configurer, method, factory, superRoot);
        root.extraUrlQueryBuilder = checkExtraUrlQueryBuilder(configurer, method, factory, superRoot);
        root.mappingBalancer = checkMappingBalancer(configurer, method, factory, superRoot);
    }

    static Charset checkAcceptCharset(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof AcceptCharsetConfigurer acceptCharsetConfigurer)
            return acceptCharsetConfigurer.acceptCharset();
        val acceptCharset = getMergedAnnotation(element, AcceptCharset.class);
        return notNullThen(acceptCharset, anno -> Charset.forName(anno.value()));
    }

    static ContentFormat.ContentFormatter checkContentFormatter(Configurer configurer,
                                                                AnnotatedElement element, Factory factory) {
        if (configurer instanceof ContentFormatConfigurer contentFormatConfigurer)
            return contentFormatConfigurer.contentFormatter();
        val contentFormat = getMergedAnnotation(element, ContentFormat.class);
        return notNullThen(contentFormat, anno -> FactoryContext.build(factory, anno.value()));
    }

    static HttpMethod checkHttpMethod(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof RequestMethodConfigurer requestMethodConfigurer)
            return requestMethodConfigurer.requestMethod();
        val requestMethod = getMergedAnnotation(element, RequestMethod.class);
        return notNullThen(requestMethod, RequestMethod::value);
    }

    static List<Pair<String, String>> checkFixedHeaders(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedHeadersConfigurer fixedHeadersConfigurer)
            return newArrayList(fixedHeadersConfigurer.fixedHeaders()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedHeader.class))
                .stream().map(anno -> Pair.of(anno.name(), cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    static List<Pair<String, String>> checkFixedPathVars(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedPathVarsConfigurer fixedPathVarsConfigurer)
            return newArrayList(fixedPathVarsConfigurer.fixedPathVars()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedPathVar.class))
                .stream().map(anno -> Pair.of(anno.name(), cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    static List<Pair<String, Object>> checkFixedParameters(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedParametersConfigurer fixedParametersConfigurer)
            return newArrayList(fixedParametersConfigurer.fixedParameters()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedParameter.class))
                .stream().map(anno -> Pair.of(anno.name(), (Object) cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    static List<Pair<String, Object>> checkFixedContexts(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof FixedContextsConfigurer fixedContextsConfigurer)
            return newArrayList(fixedContextsConfigurer.fixedContexts()).stream().filter(NOT_BLANK_KEY).toList();
        return newArrayList(getMergedRepeatableAnnotations(element, FixedContext.class))
                .stream().map(anno -> Pair.of(anno.name(), (Object) cleanupValue(
                        anno.value(), anno.emptyAsCleanup()))).filter(NOT_BLANK_KEY).toList();
    }

    private static String cleanupValue(String value, boolean emptyAsCleanup) {
        return emptyThen(value, () -> emptyAsCleanup ? null : "");
    }

    static Map<HttpStatus, Class<? extends FallbackFunction>>
    checkStatusFallbackMapping(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof StatusFallbacksConfigurer statusFallbacksConfigurer)
            return newHashMap(statusFallbacksConfigurer.statusFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusFallback.class))
                .stream().collect(toMap(StatusFallback::status, StatusFallback::fallback));
    }

    static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
    defaultFallback(Configurer configurer, Class clazz) {
        val disabled = configurer instanceof DefaultFallbackDisabledConfigurer disabledConfigurer
                ? disabledConfigurer.disabledDefaultFallback() : isAnnotated(clazz, DefaultFallbackDisabled.class);
        return disabled ? newHashMap() : of(
                HttpStatus.Series.CLIENT_ERROR, StatusErrorThrower.class,
                HttpStatus.Series.SERVER_ERROR, StatusErrorThrower.class);
    }

    static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
    checkStatusSeriesFallbackMapping(Configurer configurer, AnnotatedElement element) {
        if (configurer instanceof StatusSeriesFallbacksConfigurer statusSeriesFallbacksConfigurer)
            return newHashMap(statusSeriesFallbacksConfigurer.statusSeriesFallbackMapping());
        return newArrayList(getMergedRepeatableAnnotations(element, StatusSeriesFallback.class))
                .stream().collect(toMap(StatusSeriesFallback::statusSeries, StatusSeriesFallback::fallback));
    }

    static RequestExtend.RequestExtender checkRequestExtender(Configurer configurer,
                                                              AnnotatedElement element, Factory factory) {
        if (configurer instanceof RequestExtendConfigurer requestExtendConfigurer)
            return requestExtendConfigurer.requestExtender();
        val requestExtend = getMergedAnnotation(element, RequestExtend.class);
        return notNullThen(requestExtend, anno -> FactoryContext.build(factory, anno.value()));
    }

    static ResponseParse.ResponseParser checkResponseParser(Configurer configurer,
                                                            AnnotatedElement element, Factory factory) {
        if (configurer instanceof ResponseParseConfigurer responseParseConfigurer)
            return responseParseConfigurer.responseParser();
        val responseParse = getMergedAnnotation(element, ResponseParse.class);
        return notNullThen(responseParse, anno -> FactoryContext.build(factory, anno.value()));
    }

    static ExtraUrlQuery.ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(Configurer configurer,
                                                                        AnnotatedElement element, Factory factory) {
        if (configurer instanceof ExtraUrlQueryConfigurer extraUrlQueryConfigurer)
            return extraUrlQueryConfigurer.extraUrlQueryBuilder();
        val extraUrlQuery = getMergedAnnotation(element, ExtraUrlQuery.class);
        return notNullThen(extraUrlQuery, anno -> FactoryContext.build(factory, anno.value()));
    }

    static MappingBalance.MappingBalancer checkMappingBalancer(Configurer configurer,
                                                               AnnotatedElement element, Factory factory) {
        if (configurer instanceof MappingBalanceConfigurer mappingBalanceConfigurer)
            return mappingBalanceConfigurer.mappingBalancer();
        val mappingBalance = getMergedAnnotation(element, MappingBalance.class);
        return notNullThen(mappingBalance, anno -> FactoryContext.build(factory, anno.value()));
    }

    static RequestExtend.RequestExtender checkRequestExtender(Configurer configurer, Method method,
                                                              Factory factory, CommonRoot superRoot) {
        if (configurer instanceof RequestExtendDisabledConfigurer disabledConfigurer
                ? disabledConfigurer.disabledRequestExtend()
                : isAnnotated(method, RequestExtend.Disabled.class)) return null;
        return nullThen(checkRequestExtender(configurer, method, factory), superRoot::requestExtender);
    }

    static ResponseParse.ResponseParser checkResponseParser(Configurer configurer, Method method,
                                                            Factory factory, CommonRoot superRoot) {
        if (configurer instanceof ResponseParseDisabledConfigurer disabledConfigurer
                ? disabledConfigurer.disabledResponseParse()
                : isAnnotated(method, ResponseParse.Disabled.class)) return null;
        return nullThen(checkResponseParser(configurer, method, factory), superRoot::responseParser);
    }

    static ExtraUrlQuery.ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(Configurer configurer, Method method,
                                                                        Factory factory, CommonRoot superRoot) {
        if (configurer instanceof ExtraUrlQueryDisabledConfigurer disabledConfigurer
                ? disabledConfigurer.disabledExtraUrlQuery()
                : isAnnotated(method, ExtraUrlQuery.Disabled.class)) return null;
        return nullThen(checkExtraUrlQueryBuilder(configurer, method, factory), superRoot::extraUrlQueryBuilder);
    }

    static MappingBalance.MappingBalancer checkMappingBalancer(Configurer configurer, Method method,
                                                               Factory factory, CommonRoot superRoot) {
        return nullThen(checkMappingBalancer(configurer, method, factory), superRoot::mappingBalancer);
    }

    protected Class responseClass = CncResponse.CncResponseImpl.class;
    protected String requestBodyRaw;

    protected void processArguments(Method method, Object[] args,
                                    BiFunction<Object, Class, Boolean> process) {
        val parameterTypes = method.getParameterTypes();
        val parameters = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            val argument = args[i];
            val parameterType = parameterTypes[i];
            val parameter = parameters[i];

            val configuredType = processParameterType(argument, parameterType, process);
            if (configuredType) continue;
            processAnnotations(argument, parameter, null);
        }
    }

    private boolean processParameterType(Object argument, Class parameterType,
                                         BiFunction<Object, Class, Boolean> process) {
        if (CncRequest.class.isAssignableFrom(parameterType)) {
            this.responseClass = checkNull(argument,
                    () -> CncResponse.CncResponseImpl.class,
                    xx -> ((CncRequest) xx).responseClass());
            return false;
        }
        return process.apply(argument, parameterType);
    }

    private void processAnnotations(Object argument,
                                    AnnotatedElement annotatedElement,
                                    String defaultParameterName) {
        final AtomicBoolean processed = new AtomicBoolean(false);
        notNullThenRun(findAnnotation(annotatedElement, Header.class), header -> {
            processHeader(argument, header);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, PathVar.class), pathVar -> {
            processPathVar(argument, pathVar);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, Parameter.class), parameter -> {
            processParameter(argument, parameter);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, Context.class), context -> {
            processContext(argument, context);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, RequestBodyRaw.class), xx -> {
            processRequestBodyRaw(argument);
            processed.set(true);
        });
        notNullThenRun(findAnnotation(annotatedElement, Bundle.class), xx -> {
            processBundle(argument);
            processed.set(true);
        });
        if (!processed.get() && nonNull(defaultParameterName)) {
            processParameter(argument, new ParameterImpl(defaultParameterName));
        }
    }

    private void processHeader(Object argument, Header header) {
        this.headers.add(Pair.of(header.value(),
                notNullThen(argument, Str::toStr)));
    }

    private void processPathVar(Object argument, PathVar pathVar) {
        this.pathVars.add(Pair.of(pathVar.value(),
                notNullThen(argument, Str::toStr)));
    }

    private void processParameter(Object argument, Parameter parameter) {
        this.parameters.add(Pair.of(parameter.value(), argument));
    }

    private void processContext(Object argument, Context context) {
        this.contexts.add(Pair.of(context.value(), argument));
    }

    private void processRequestBodyRaw(Object argument) {
        if (null == argument || (argument instanceof String)) {
            // OhRequestBodyRaw参数传值null时, 则继续使用parameters构造请求
            this.requestBodyRaw = (String) argument;
            return;
        }
        log.warn("Argument annotated with @RequestBodyRaw, " +
                "but Type is {} instead String.", argument.getClass());
    }

    private void processBundle(Object argument) {
        if (isNull(argument)) return;
        if (argument instanceof Map) {
            desc(argument).forEach((key, value) ->
                    processParameter(value, new ParameterImpl(toStr(key))));
            return;
        }

        val clazz = argument.getClass();
        val reflect = on(argument);
        val fields = reflect.fields();
        for (val fieldEntry : fields.entrySet()) {
            val fieldName = fieldEntry.getKey();
            processAnnotations(fieldEntry.getValue().get(),
                    Reflectt.field0(clazz, fieldName), fieldName);
        }
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    @AllArgsConstructor
    private static class ParameterImpl implements Parameter {

        @Getter
        @Accessors(fluent = true)
        private final Class<? extends Annotation> annotationType = Parameter.class;
        private String value;

        @Override
        public String value() {
            return value;
        }
    }
}
