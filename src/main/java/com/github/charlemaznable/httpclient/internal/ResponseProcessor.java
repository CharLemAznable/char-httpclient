package com.github.charlemaznable.httpclient.internal;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.httpclient.common.CncResponse;
import com.github.charlemaznable.httpclient.common.FallbackFunction;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.codec.Json.desc;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static java.util.Objects.nonNull;

@SuppressWarnings("rawtypes")
public abstract class ResponseProcessor<R/* Response Type */, B/* Response Body Type */> {

    private static final String RETURN_GENERIC_ERROR = "Method return type generic Error";

    @Setter
    Factory factory;
    @Getter
    @Setter
    CommonRoot root;

    @Getter
    boolean returnFuture; // Future<V>
    boolean returnCollection; // Collection<E>
    boolean returnMap; // Map<K, V>
    boolean returnPair; // Pair<L, R>
    boolean returnTriple; // Triple<L, M, R>
    List<Class> returnTypes;

    public void processReturnType(Method method, Class<?> futureClass) {
        Class<?> returnType = method.getReturnType();
        this.returnFuture = futureClass == returnType;
        this.returnCollection = Collection.class.isAssignableFrom(returnType);
        this.returnMap = Map.class.isAssignableFrom(returnType);
        this.returnPair = Pair.class.isAssignableFrom(returnType);
        this.returnTriple = Triple.class.isAssignableFrom(returnType);

        val genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            // 错误的泛型时
            if (this.returnFuture || this.returnCollection ||
                    this.returnPair || this.returnTriple) {
                // 如返回支持的泛型类型则抛出异常
                // 不包括Map<K, V>
                throw new IllegalStateException(RETURN_GENERIC_ERROR);
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
            throw new IllegalStateException(RETURN_GENERIC_ERROR);
        }
    }

    public Object processResponse(R response, Class responseClass,
                                  List<Pair<String, Object>> contexts) {
        val statusCode = getResponseCode(response);
        val responseBody = getResponseBody(response);

        val statusFallback = root.statusFallbackMapping
                .get(HttpStatus.valueOf(statusCode));
        if (nonNull(statusFallback)) {
            return applyFallback(statusFallback, statusCode, responseBody);
        }

        val statusSeriesFallback = root.statusSeriesFallbackMapping
                .get(HttpStatus.Series.valueOf(statusCode));
        if (nonNull(statusSeriesFallback)) {
            return applyFallback(statusSeriesFallback, statusCode, responseBody);
        }

        val responseObjs = processResponseBody(
                statusCode, responseBody, responseClass, contexts);
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

    protected abstract int getResponseCode(R response);

    protected abstract B getResponseBody(R response);

    private Object applyFallback(Class<? extends FallbackFunction> function,
                                 int statusCode, B responseBody) {
        return FactoryContext.apply(factory, function,
                f -> f.apply(new FallbackFunction.Response<>(statusCode, responseBody) {
                    @Override
                    public String responseBodyAsString() {
                        return toStr(notNullThen(getResponseBody(),
                                body -> responseBodyToString(body)));
                    }
                }));
    }

    protected abstract String responseBodyToString(B responseBody);

    private List<Object> processResponseBody(int statusCode,
                                             B responseBody,
                                             Class responseClass,
                                             List<Pair<String, Object>> contexts) {
        return this.returnTypes.stream().map(returnType ->
                        processReturnTypeValue(statusCode, responseBody,
                                CncResponse.class == returnType ? responseClass : returnType, contexts))
                .collect(Collectors.toList());
    }

    private Object processReturnTypeValue(int statusCode,
                                          B responseBody,
                                          Class returnType,
                                          List<Pair<String, Object>> contexts) {
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
        } else {
            return customProcessReturnTypeValue(statusCode, responseBody, returnType, contexts);
        }
    }

    protected abstract Object customProcessReturnTypeValue(int statusCode,
                                                           B responseBody,
                                                           Class returnType,
                                                           List<Pair<String, Object>> contexts);

    private boolean returnVoid(Class returnType) {
        return void.class == returnType || Void.class == returnType;
    }

    private boolean returnInteger(Class returnType) {
        return int.class == returnType || Integer.class == returnType;
    }

    private boolean returnBoolean(Class returnType) {
        return boolean.class == returnType || Boolean.class == returnType;
    }

    protected boolean returnUnCollectionString(Class returnType) {
        return String.class == returnType && !this.returnCollection;
    }
}
