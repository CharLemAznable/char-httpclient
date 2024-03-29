package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.core.lang.Reflectt;
import com.github.charlemaznable.core.lang.Str;
import com.github.charlemaznable.core.mutiny.MutinyBuildHelper;
import com.github.charlemaznable.core.reactor.ReactorBuildHelper;
import com.github.charlemaznable.core.rxjava.RxJava1BuildHelper;
import com.github.charlemaznable.core.rxjava.RxJava2BuildHelper;
import com.github.charlemaznable.core.rxjava.RxJava3BuildHelper;
import com.github.charlemaznable.httpclient.annotation.Bundle;
import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.Context;
import com.github.charlemaznable.httpclient.annotation.ExtraUrlQuery;
import com.github.charlemaznable.httpclient.annotation.Header;
import com.github.charlemaznable.httpclient.annotation.MappingBalance;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.annotation.PathVar;
import com.github.charlemaznable.httpclient.annotation.RequestBodyRaw;
import com.github.charlemaznable.httpclient.annotation.RequestExtend;
import com.github.charlemaznable.httpclient.annotation.ResponseParse;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceDecorators;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.text.StringSubstitutor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.codec.Json.desc;
import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.spec;
import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.codec.Json.unJsonArray;
import static com.github.charlemaznable.core.codec.Xml.unXml;
import static com.github.charlemaznable.core.lang.Clz.isAssignable;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.toMap;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.joor.Reflect.on;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public abstract class CommonExecute<T extends CommonBase<T>, M extends CommonMethod<T>,
        R/* Response Type */, A extends CommonResponseAdapter<R, ?, ?>> {

    final T base;
    final M executeMethod;

    Object[] args;
    Class<? extends CncResponse> responseClass = CncResponse.CncResponseImpl.class;
    String requestBodyRaw;

    public final void prepareArguments(Object[] args) {
        this.args = args;
        val parameterTypes = executeMethod.method().getParameterTypes();
        val parameters = executeMethod.method().getParameters();
        for (int i = 0; i < args.length; i++) {
            val argument = args[i];
            val parameterType = parameterTypes[i];
            val parameter = parameters[i];

            val configuredType = processParameterType(argument, parameterType);
            if (configuredType) continue;
            processAnnotations(argument, parameter, null);
        }
    }

    protected boolean processParameterType(Object argument, Class<?> parameterType) {
        if (argument instanceof Charset acceptCharset) {
            base.acceptCharset = acceptCharset;
        } else if (argument instanceof ContentFormat.ContentFormatter contentFormatter) {
            base.contentFormatter = contentFormatter;
        } else if (argument instanceof HttpMethod httpMethod) {
            base.httpMethod = httpMethod;
        } else if (isAssignable(parameterType, RequestExtend.RequestExtender.class)) {
            base.requestExtender = (RequestExtend.RequestExtender) argument;
        } else if (isAssignable(parameterType, ResponseParse.ResponseParser.class)) {
            base.responseParser = (ResponseParse.ResponseParser) argument;
        } else if (isAssignable(parameterType, ExtraUrlQuery.ExtraUrlQueryBuilder.class)) {
            base.extraUrlQueryBuilder = (ExtraUrlQuery.ExtraUrlQueryBuilder) argument;
        } else if (argument instanceof MappingBalance.MappingBalancer mappingBalancer) {
            base.mappingBalancer = mappingBalancer;
        } else if (isAssignable(parameterType, Bulkhead.class)) {
            base.resilienceBase.bulkhead((Bulkhead) argument);
        } else if (isAssignable(parameterType, TimeLimiter.class)) {
            base.resilienceBase.timeLimiter((TimeLimiter) argument);
        } else if (isAssignable(parameterType, RateLimiter.class)) {
            base.resilienceBase.rateLimiter((RateLimiter) argument);
        } else if (isAssignable(parameterType, CircuitBreaker.class)) {
            base.resilienceBase.circuitBreaker((CircuitBreaker) argument);
        } else if (isAssignable(parameterType, Retry.class)) {
            base.resilienceBase.retry((Retry) argument);
        } else if (isAssignable(parameterType, CncRequest.class)) {
            this.responseClass = checkNull(argument,
                    () -> CncResponse.CncResponseImpl.class,
                    xx -> ((CncRequest<? extends CncResponse>) xx).responseClass());
            return false;
        } else {
            return false;
        }
        return true;
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
        base.headers.add(Pair.of(header.value(),
                notNullThen(argument, Str::toStr)));
    }

    private void processPathVar(Object argument, PathVar pathVar) {
        base.pathVars.add(Pair.of(pathVar.value(),
                notNullThen(argument, Str::toStr)));
    }

    private void processParameter(Object argument, Parameter parameter) {
        base.parameters.add(Pair.of(parameter.value(), argument));
    }

    private void processContext(Object argument, Context context) {
        base.contexts.add(Pair.of(context.value(), argument));
    }

    private void processRequestBodyRaw(Object argument) {
        if (null == argument || (argument instanceof String)) {
            // OhRequestBodyRaw参数传值null时, 则继续使用parameters构造请求
            this.requestBodyRaw = (String) argument;
            return;
        }
        executeMethod.defaultClass.logger.warn(
                "Argument annotated with @RequestBodyRaw, " +
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

    public abstract Object execute();

    @Getter
    @Accessors(fluent = true)
    protected static final class CommonExecuteParams {
        private Map<String, Object> parameterMap;
        private Map<String, Object> contextMap;
        private String requestUrl;
        private String requestMethod;
    }

    protected final CommonExecuteParams buildCommonExecuteParams() {
        val params = new CommonExecuteParams();
        val pathVarMap = base.pathVars.stream().collect(toMap(Pair::getKey, Pair::getValue));
        params.parameterMap = base.parameters.stream().collect(toMap(Pair::getKey, Pair::getValue));
        params.contextMap = base.contexts.stream().collect(toMap(Pair::getKey, Pair::getValue));
        val pathVarSubstitutor = new StringSubstitutor(pathVarMap, "{", "}");
        val substitutedUrl = pathVarSubstitutor.replace(
                base.mappingBalancer.choose(executeMethod().requestUrls));
        val extraUrlQuery = checkNull(base.extraUrlQueryBuilder,
                () -> "", builder -> builder.build(params.parameterMap, params.contextMap));
        params.requestUrl = concatUrlQuery(substitutedUrl, extraUrlQuery);
        params.requestMethod = base.httpMethod.toString();
        return params;
    }

    public final Object processResponse(R response) {
        val responseAdapter = responseAdapter(response);
        val statusCode = responseAdapter.statusCode();

        val statusFallback = base.statusFallbackMapping
                .get(HttpStatus.valueOf(statusCode));
        if (nonNull(statusFallback)) {
            return applyFallback(statusFallback, responseAdapter);
        }

        val statusSeriesFallback = base.statusSeriesFallbackMapping
                .get(HttpStatus.Series.valueOf(statusCode));
        if (nonNull(statusSeriesFallback)) {
            return applyFallback(statusSeriesFallback, responseAdapter);
        }

        val responseObjs = processResponseBody(responseAdapter, responseClass);
        if (executeMethod.returnList) {
            val responseObj = responseObjs.get(0);
            if (responseObj instanceof Collection) {
                return newArrayList((Collection<?>) responseObj);
            } else {
                return newArrayList(responseObj);
            }
        } else if (executeMethod.returnMap) {
            val responseObj = responseObjs.get(0);
            if (responseObj instanceof Map) {
                return newHashMap((Map<?, ?>) responseObj);
            } else {
                return unJson(json(responseObj), HashMap.class);
            }
        } else if (executeMethod.returnPair) {
            return Pair.of(responseObjs.get(0),
                    responseObjs.get(1));
        } else if (executeMethod.returnTriple) {
            return Triple.of(responseObjs.get(0),
                    responseObjs.get(1), responseObjs.get(2));
        } else {
            return responseObjs.get(0);
        }
    }

    protected abstract A responseAdapter(R response);

    private Object applyFallback(FallbackFunction<?> function, A responseAdapter) {
        return function.apply(responseAdapter.buildCommonResponse());
    }

    private List<Object> processResponseBody(A responseAdapter, Class<?> responseClass) {
        return executeMethod.returnTypes.stream().map(returnType ->
                        processReturnTypeValue(responseAdapter,
                                CncResponse.class == returnType ? responseClass : returnType))
                .collect(Collectors.toList());
    }

    private Object processReturnTypeValue(A responseAdapter, Class<?> returnType) {
        if (returnVoid(returnType)) {
            return null;
        } else if (returnInteger(returnType)) {
            return responseAdapter.statusCode();
        } else if (HttpStatus.class == returnType) {
            return HttpStatus.valueOf(responseAdapter.statusCode());
        } else if (HttpStatus.Series.class == returnType) {
            return HttpStatus.Series.valueOf(responseAdapter.statusCode());
        } else if (returnBoolean(returnType)) {
            return HttpStatus.valueOf(responseAdapter.statusCode()).is2xxSuccessful();
        } else if (HttpHeaders.class == returnType) {
            return responseAdapter.buildHttpHeaders();
        } else if (returnUnCollectionString(returnType)) {
            return responseAdapter.buildBodyString();
        } else if (CommonResponse.class == returnType) {
            return responseAdapter.buildCommonResponse();
        } else {
            return customProcessReturnTypeValue(responseAdapter, returnType);
        }
    }

    protected Object customProcessReturnTypeValue(A responseAdapter, Class<?> returnType) {
        return parseObject(responseAdapter, returnType);
    }

    private Object parseObject(A responseAdapter, Class<?> returnType) {
        if (nonNull(base.responseParser)) {
            val commonResponse = responseAdapter.buildCommonResponse();
            val contextMap = base.contexts.stream().collect(toMap(Pair::getKey, Pair::getValue));
            return base.responseParser.parse(commonResponse, returnType, contextMap);
        }
        val content = responseAdapter.buildBodyString();
        if (isBlank(content)) return null;
        if (content.startsWith("<")) return spec(unXml(content), returnType);
        if (content.startsWith("[")) return unJsonArray(content, returnType);
        if (content.startsWith("{")) return unJson(content, returnType);
        throw new IllegalArgumentException("Parse response body Error: \n" + content);
    }

    private boolean returnVoid(Class<?> returnType) {
        return void.class == returnType || Void.class == returnType;
    }

    private boolean returnInteger(Class<?> returnType) {
        return int.class == returnType || Integer.class == returnType;
    }

    private boolean returnBoolean(Class<?> returnType) {
        return boolean.class == returnType || Boolean.class == returnType;
    }

    private boolean returnUnCollectionString(Class<?> returnType) {
        return String.class == returnType && !executeMethod.returnList;
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    protected Object adaptationFromFuture(CompletableFuture<Object> future) {
        if (executeMethod.returnReactorMono()) {
            return ReactorBuildHelper.buildMonoFromFuture(future);
        } else if (executeMethod.returnRxJavaSingle()) {
            return RxJava1BuildHelper.buildSingleFromFuture(future);
        } else if (executeMethod.returnRxJava2Single()) {
            return RxJava2BuildHelper.buildSingleFromFuture(future);
        } else if (executeMethod.returnRxJava3Single()) {
            return RxJava3BuildHelper.buildSingleFromFuture(future);
        } else if (executeMethod.returnMutinyUni()) {
            return MutinyBuildHelper.buildUniFromFuture(future);
        } else {
            return future;
        }
    }

    protected CompletableFuture<Object> decorateAsyncExecute(Supplier<CompletionStage<Object>> stageSupplier) {
        return ResilienceDecorators.ofCompletionStage(stageSupplier)
                .withResilienceBase(base.resilienceBase).get().toCompletableFuture();
    }
}
