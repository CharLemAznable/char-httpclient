package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CncResponse;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.common.ExtraUrlQuery.ExtraUrlQueryBuilder;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.FallbackFunction.Response;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.RequestExtend;
import com.github.charlemaznable.httpclient.common.RequestExtend.RequestExtender;
import com.github.charlemaznable.httpclient.common.ResponseParse;
import com.github.charlemaznable.httpclient.common.ResponseParse.ResponseParser;
import com.github.charlemaznable.httpclient.configurer.Configurer;
import com.github.charlemaznable.httpclient.configurer.ExtraUrlQueryDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.RequestExtendDisabledConfigurer;
import com.github.charlemaznable.httpclient.configurer.ResponseParseDisabledConfigurer;
import com.github.charlemaznable.httpclient.ohclient.OhException;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientInterceptorCleanup;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientProxy;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientSSL;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientInterceptorsCleanupConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientProxyDisabledConfigurer;
import com.github.charlemaznable.httpclient.ohclient.configurer.ClientSSLDisabledConfigurer;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.codec.Json.desc;
import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.httpclient.ohclient.internal.OhDummy.ohExecutorService;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@SuppressWarnings("rawtypes")
public final class OhMappingProxy extends OhRoot {

    private static final String RETURN_GENERIC_ERROR = "Method return type generic Error";

    Class ohClass;
    Method ohMethod;
    Factory factory;
    Configurer configurer;
    List<String> requestUrls;

    boolean returnFuture; // Future<V>
    boolean returnCollection; // Collection<E>
    boolean returnMap; // Map<K, V>
    boolean returnPair; // Pair<L, R>
    boolean returnTriple; // Triple<L, M, R>
    List<Class> returnTypes;

    OhMappingProxy(Class ohClass, Method ohMethod,
                   Factory factory, OhProxy proxy) {
        this.ohClass = ohClass;
        this.ohMethod = ohMethod;
        this.factory = factory;
        this.configurer = checkConfigurer(this.ohMethod, this.factory);

        this.requestUrls = Elf.checkRequestUrls(this.configurer, this.ohMethod, proxy);

        this.clientProxy = Elf.checkClientProxy(this.configurer, this.ohMethod, proxy);
        this.sslRoot = Elf.checkClientSSL(this.configurer, this.ohMethod, this.factory, proxy);
        this.connectionPool = nullThen(checkConnectionPool(
                this.configurer, this.ohMethod), () -> proxy.connectionPool);
        this.timeoutRoot = Elf.checkClientTimeout(this.configurer, this.ohMethod, proxy);
        this.interceptors = Elf.defaultClientInterceptors(this.configurer, this.ohMethod, proxy);
        this.interceptors.addAll(checkClientInterceptors(this.configurer, this.ohMethod, this.factory));
        this.loggingLevel = nullThen(checkClientLoggingLevel(
                this.configurer, this.ohMethod), () -> proxy.loggingLevel);

        this.okHttpClient = Elf.buildOkHttpClient(this, proxy);

        this.acceptCharset = nullThen(checkAcceptCharset(
                this.configurer, this.ohMethod), () -> proxy.acceptCharset);
        this.contentFormatter = nullThen(checkContentFormatter(
                this.configurer, this.ohMethod, this.factory), () -> proxy.contentFormatter);
        this.httpMethod = nullThen(checkHttpMethod(
                this.configurer, this.ohMethod), () -> proxy.httpMethod);
        this.headers = newArrayList(proxy.headers);
        this.headers.addAll(checkFixedHeaders(this.configurer, this.ohMethod));
        this.pathVars = newArrayList(proxy.pathVars);
        this.pathVars.addAll(checkFixedPathVars(this.configurer, this.ohMethod));
        this.parameters = newArrayList(proxy.parameters);
        this.parameters.addAll(checkFixedParameters(this.configurer, this.ohMethod));
        this.contexts = newArrayList(proxy.contexts);
        this.contexts.addAll(checkFixedContexts(this.configurer, this.ohMethod));

        this.statusFallbackMapping = newHashMap(proxy.statusFallbackMapping);
        this.statusFallbackMapping.putAll(checkStatusFallbackMapping(this.configurer, this.ohMethod));
        this.statusSeriesFallbackMapping = newHashMap(proxy.statusSeriesFallbackMapping);
        this.statusSeriesFallbackMapping.putAll(checkStatusSeriesFallbackMapping(this.configurer, this.ohMethod));

        this.requestExtender = Elf.checkRequestExtender(this.configurer, this.ohMethod, this.factory, proxy);
        this.responseParser = Elf.checkResponseParser(this.configurer, this.ohMethod, this.factory, proxy);
        this.extraUrlQueryBuilder = Elf.checkExtraUrlQueryBuilder(this.configurer, this.ohMethod, this.factory, proxy);
        this.mappingBalancer = nullThen(checkMappingBalancer(
                this.configurer, this.ohMethod, this.factory), () -> proxy.mappingBalancer);

        processReturnType(this.ohMethod);
    }

    Object execute(Object[] args) {
        if (this.returnFuture) {
            return ohExecutorService.submit(
                    () -> internalExecute(args));
        }
        return internalExecute(args);
    }

    private void processReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        this.returnFuture = Future.class == returnType;
        this.returnCollection = Collection.class.isAssignableFrom(returnType);
        this.returnMap = Map.class.isAssignableFrom(returnType);
        this.returnPair = Pair.class.isAssignableFrom(returnType);
        this.returnTriple = Triple.class.isAssignableFrom(returnType);

        val genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            // 错误的泛型时
            if (this.returnFuture || this.returnCollection ||
                    this.returnPair || this.returnTriple) {
                // 如返回支持的泛型类型则抛出异常
                // 不包括Map<K, V>
                throw new OhException(RETURN_GENERIC_ERROR);
            } else if (genericReturnType instanceof TypeVariable) {
                // 返回类型变量指定的类型时
                // 检查是否为<T extend CncResponse>类型
                checkTypeVariableBounds(genericReturnType);
                this.returnTypes = newArrayList(CncResponse.class);
                return;
            } else {
                // 否则以方法返回类型作为实际返回类型
                // 返回Map时, 可直接解析返回值为Map
                this.returnTypes = newArrayList(returnType);
                return;
            }
        }

        // 方法返回泛型时
        ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (this.returnFuture) {
            // 返回Future类型, 则多处理一层泛型
            val futureTypeArgument = actualTypeArguments[0];
            if (!(futureTypeArgument instanceof ParameterizedType)) {
                if (futureTypeArgument instanceof TypeVariable) {
                    checkTypeVariableBounds(futureTypeArgument);
                    this.returnTypes = newArrayList(CncResponse.class);
                    return;
                }
                this.returnTypes = newArrayList((Class) futureTypeArgument);
                return;
            }

            parameterizedType = (ParameterizedType) futureTypeArgument;
            returnType = (Class) parameterizedType.getRawType();
            this.returnCollection = Collection.class.isAssignableFrom(returnType);
            this.returnMap = Map.class.isAssignableFrom(returnType);
            this.returnPair = Pair.class.isAssignableFrom(returnType);
            this.returnTriple = Triple.class.isAssignableFrom(returnType);
            actualTypeArguments = parameterizedType.getActualTypeArguments();
        }
        if (this.returnCollection || this.returnPair || this.returnTriple) {
            // 以泛型参数类型作为返回值解析目标类型
            this.returnTypes = processActualTypeArguments(actualTypeArguments);
        } else {
            // 以泛型类型作为返回值解析目标类型
            this.returnTypes = newArrayList(returnType);
        }
    }

    private List<Class> processActualTypeArguments(Type[] actualTypeArguments) {
        List<Class> result = newArrayList();
        for (Type actualTypeArgument : actualTypeArguments) {
            if (actualTypeArgument instanceof TypeVariable) {
                checkTypeVariableBounds(actualTypeArgument);
                result.add(CncResponse.class);
                continue;
            }
            result.add((Class) actualTypeArgument);
        }
        return result;
    }

    private void checkTypeVariableBounds(Type type) {
        val bounds = ((TypeVariable) type).getBounds();
        if (bounds.length != 1 || !CncResponse.class
                .isAssignableFrom((Class) bounds[0])) {
            throw new OhException(RETURN_GENERIC_ERROR);
        }
    }

    @SneakyThrows
    private Object internalExecute(Object[] args) {
        val ohCall = new OhCall(this, args);
        val response = ohCall.execute();

        val statusCode = response.code();
        val responseBody = notNullThen(response.body(), OhResponseBody::new);
        if (nonNull(response.body())) response.close();

        val statusFallback = this.statusFallbackMapping
                .get(HttpStatus.valueOf(statusCode));
        if (nonNull(statusFallback)) {
            return applyFallback(statusFallback, statusCode, responseBody);
        }

        val statusSeriesFallback = this.statusSeriesFallbackMapping
                .get(HttpStatus.Series.valueOf(statusCode));
        if (nonNull(statusSeriesFallback)) {
            return applyFallback(statusSeriesFallback, statusCode, responseBody);
        }

        val responseObjs = processResponseBody(
                statusCode, responseBody, ohCall.responseClass);
        if (this.returnCollection) {
            val responseObj = responseObjs.get(0);
            if (responseObj instanceof Collection) {
                return newArrayList((Collection) responseObj);
            } else {
                return newArrayList(responseObj);
            }

        } else if (this.returnMap) {
            val responseObj = responseObjs.get(0);
            if (responseObj instanceof Map) {
                return newHashMap((Map) responseObj);
            } else {
                return desc(responseObj);
            }

        } else if (this.returnPair) {
            return Pair.of(responseObjs.get(0),
                    responseObjs.get(1));

        } else if (this.returnTriple) {
            return Triple.of(responseObjs.get(0),
                    responseObjs.get(1), responseObjs.get(2));

        } else {
            return responseObjs.get(0);
        }
    }

    private Object applyFallback(Class<? extends FallbackFunction> function,
                                 int statusCode, ResponseBody responseBody) {
        return FactoryContext.apply(factory, function,
                f -> f.apply(new Response<ResponseBody>(statusCode, responseBody) {
                    @Override
                    public String responseBodyAsString() {
                        return toStr(notNullThen(getResponseBody(),
                                ResponseBodyExtractor::string));
                    }
                }));
    }

    private List<Object> processResponseBody(int statusCode,
                                             ResponseBody responseBody,
                                             Class responseClass) {
        return this.returnTypes.stream().map(returnType ->
                        processReturnTypeValue(statusCode, responseBody,
                                CncResponse.class == returnType ? responseClass : returnType))
                .collect(Collectors.toList());
    }

    private Object processReturnTypeValue(int statusCode,
                                          ResponseBody responseBody,
                                          Class returnType) {
        if (returnVoid(returnType)) {
            return null;
        } else if (returnInteger(returnType)) {
            return statusCode;
        } else if (HttpStatus.class == returnType) {
            return HttpStatus.valueOf(statusCode);
        } else if (HttpStatus.Series.class == returnType) {
            return HttpStatus.Series.valueOf(statusCode);
        } else if (returnBoolean(returnType)) {
            return HttpStatus.valueOf(statusCode).is2xxSuccessful();
        } else if (ResponseBody.class.isAssignableFrom(returnType)) {
            return responseBody;
        } else if (InputStream.class == returnType) {
            return notNullThen(responseBody, ResponseBodyExtractor::byteStream);
        } else if (BufferedSource.class.isAssignableFrom(returnType)) {
            return (notNullThen(responseBody, ResponseBodyExtractor::source));
        } else if (byte[].class == returnType) {
            return notNullThen(responseBody, ResponseBodyExtractor::bytes);
        } else if (Reader.class.isAssignableFrom(returnType)) {
            return notNullThen(responseBody, ResponseBodyExtractor::charStream);
        } else if (returnUnCollectionString(returnType)) {
            return notNullThen(responseBody, ResponseBodyExtractor::string);
        } else {
            return notNullThen(responseBody, body ->
                    ResponseBodyExtractor.object(body, notNullThen(this.responseParser, parser -> {
                        val contextMap = this.contexts.stream().collect(toMap(Pair::getKey, Pair::getValue));
                        return content -> parser.parse(content, returnType, contextMap);
                    }), returnType));
        }
    }

    private boolean returnVoid(Class returnType) {
        return void.class == returnType || Void.class == returnType;
    }

    private boolean returnInteger(Class returnType) {
        return int.class == returnType || Integer.class == returnType;
    }

    private boolean returnBoolean(Class returnType) {
        return boolean.class == returnType || Boolean.class == returnType;
    }

    private boolean returnUnCollectionString(Class returnType) {
        return String.class == returnType && !this.returnCollection;
    }

    @NoArgsConstructor(access = PRIVATE)
    static class Elf {

        static List<String> checkRequestUrls(Configurer configurer, Method method, OhProxy proxy) {
            val urls = emptyThen(checkMappingUrls(configurer, method), () ->
                    newArrayList(proxy.mappingMethodNameDisabled ? "" : "/" + method.getName()));
            Set<String> requestUrls = newHashSet();
            for (val url : urls) {
                if (isBlank(url)) {
                    requestUrls.addAll(proxy.baseUrls);
                } else {
                    requestUrls.addAll(proxy.baseUrls.stream()
                            .map(base -> checkBlank(base, () -> url, b -> b + url))
                            .collect(Collectors.toList()));
                }
            }
            return requestUrls.stream().distinct().collect(Collectors.toList());
        }

        static Proxy checkClientProxy(Configurer configurer, Method method, OhProxy proxy) {
            if (configurer instanceof ClientProxyDisabledConfigurer
                    ? ((ClientProxyDisabledConfigurer) configurer).disabledClientProxy()
                    : isAnnotated(method, ClientProxy.Disabled.class)) return null;
            return nullThen(OhRoot.checkClientProxy(configurer, method), () -> proxy.clientProxy);
        }

        static SSLRoot checkClientSSL(Configurer configurer, Method method, Factory factory, OhProxy proxy) {
            boolean disabledSSLSocketFactory;
            boolean disabledX509TrustManager;
            boolean disabledHostnameVerifier;
            if (configurer instanceof ClientSSLDisabledConfigurer) {
                val disabledConfigurer = (ClientSSLDisabledConfigurer) configurer;
                disabledSSLSocketFactory = disabledConfigurer.disabledSSLSocketFactory();
                disabledX509TrustManager = disabledConfigurer.disabledX509TrustManager();
                disabledHostnameVerifier = disabledConfigurer.disabledHostnameVerifier();
            } else {
                disabledSSLSocketFactory = isAnnotated(method, ClientSSL.DisabledSSLSocketFactory.class);
                disabledX509TrustManager = isAnnotated(method, ClientSSL.DisabledX509TrustManager.class);
                disabledHostnameVerifier = isAnnotated(method, ClientSSL.DisabledHostnameVerifier.class);
            }
            return OhRoot.checkClientSSL(configurer, method, factory,
                    disabledSSLSocketFactory, disabledX509TrustManager, disabledHostnameVerifier, proxy.sslRoot);
        }

        static TimeoutRoot checkClientTimeout(Configurer configurer, Method method, OhProxy proxy) {
            return OhRoot.checkClientTimeout(configurer, method, proxy.timeoutRoot);
        }

        static List<Interceptor> defaultClientInterceptors(Configurer configurer, Method method, OhProxy proxy) {
            val cleanup = configurer instanceof ClientInterceptorsCleanupConfigurer
                    ? ((ClientInterceptorsCleanupConfigurer) configurer).cleanupInterceptors()
                    : isAnnotated(method, ClientInterceptorCleanup.class);
            return newArrayList(cleanup ? null : proxy.interceptors);
        }

        static OkHttpClient buildOkHttpClient(OhMappingProxy mappingProxy, OhProxy proxy) {
            val sameClientProxy = mappingProxy.clientProxy == proxy.clientProxy;
            val sameSSLSocketFactory = mappingProxy.sslRoot.sslSocketFactory == proxy.sslRoot.sslSocketFactory;
            val sameX509TrustManager = mappingProxy.sslRoot.x509TrustManager == proxy.sslRoot.x509TrustManager;
            val sameHostnameVerifier = mappingProxy.sslRoot.hostnameVerifier == proxy.sslRoot.hostnameVerifier;
            val sameConnectionPool = mappingProxy.connectionPool == proxy.connectionPool;
            val sameCallTimeout = mappingProxy.timeoutRoot.callTimeout == proxy.timeoutRoot.callTimeout;
            val sameConnectTimeout = mappingProxy.timeoutRoot.connectTimeout == proxy.timeoutRoot.connectTimeout;
            val sameReadTimeout = mappingProxy.timeoutRoot.readTimeout == proxy.timeoutRoot.readTimeout;
            val sameWriteTimeout = mappingProxy.timeoutRoot.writeTimeout == proxy.timeoutRoot.writeTimeout;
            val sameInterceptors = mappingProxy.interceptors.equals(proxy.interceptors);
            val sameLoggingLevel = mappingProxy.loggingLevel == proxy.loggingLevel;
            if (sameClientProxy && sameSSLSocketFactory && sameX509TrustManager
                    && sameHostnameVerifier && sameConnectionPool
                    && sameCallTimeout && sameConnectTimeout
                    && sameReadTimeout && sameWriteTimeout
                    && sameInterceptors && sameLoggingLevel) return proxy.okHttpClient;

            return OhRoot.buildOkHttpClient(mappingProxy);
        }

        static RequestExtender checkRequestExtender(
                Configurer configurer, Method method, Factory factory, OhProxy proxy) {
            if (configurer instanceof RequestExtendDisabledConfigurer
                    ? ((RequestExtendDisabledConfigurer) configurer).disabledRequestExtend()
                    : isAnnotated(method, RequestExtend.Disabled.class)) return null;
            return nullThen(OhRoot.checkRequestExtender(
                    configurer, method, factory), () -> proxy.requestExtender);
        }

        static ResponseParser checkResponseParser(
                Configurer configurer, Method method, Factory factory, OhProxy proxy) {
            if (configurer instanceof ResponseParseDisabledConfigurer
                    ? ((ResponseParseDisabledConfigurer) configurer).disabledResponseParse()
                    : isAnnotated(method, ResponseParse.Disabled.class)) return null;
            return nullThen(OhRoot.checkResponseParser(
                    configurer, method, factory), () -> proxy.responseParser);
        }

        static ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(
                Configurer configurer, Method method, Factory factory, OhProxy proxy) {
            if (configurer instanceof ExtraUrlQueryDisabledConfigurer
                    ? ((ExtraUrlQueryDisabledConfigurer) configurer).disabledExtraUrlQuery()
                    : isAnnotated(method, ExtraUrlQuery.Disabled.class)) return null;
            return nullThen(OhRoot.checkExtraUrlQueryBuilder(
                    configurer, method, factory), () -> proxy.extraUrlQueryBuilder);
        }
    }
}
