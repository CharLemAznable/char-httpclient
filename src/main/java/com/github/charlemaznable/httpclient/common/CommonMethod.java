package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.core.mutiny.MutinyCheckHelper;
import com.github.charlemaznable.core.reactor.ReactorCheckHelper;
import com.github.charlemaznable.core.rxjava.RxJavaCheckHelper;
import lombok.Getter;
import lombok.experimental.Accessors;
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
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Str.isBlank;

public abstract class CommonMethod<T extends CommonBase<T>> implements Reloadable {

    @Getter
    @Accessors(fluent = true)
    final CommonElement<T> element;
    @Getter
    @Accessors(fluent = true)
    final CommonClass<T> defaultClass;
    @Getter
    @Accessors(fluent = true)
    final Method method;

    List<String> requestUrls;

    static final String RETURN_GENERIC_ERROR = "Method return type generic Error";

    @Getter
    @Accessors(fluent = true)
    boolean returnFuture; // Future<V>, etc.
    // async return types check start
    @Getter
    @Accessors(fluent = true)
    boolean returnJavaFuture;
    @Getter
    @Accessors(fluent = true)
    boolean returnReactorMono;
    @Getter
    @Accessors(fluent = true)
    boolean returnRxJavaSingle;
    @Getter
    @Accessors(fluent = true)
    boolean returnRxJava2Single;
    @Getter
    @Accessors(fluent = true)
    boolean returnRxJava3Single;
    @Getter
    @Accessors(fluent = true)
    boolean returnMutinyUni;
    // async return types check finish
    boolean returnCollection; // Collection<E>
    boolean returnMap; // Map<K, V>
    boolean returnPair; // Pair<L, R>
    boolean returnTriple; // Triple<L, M, R>
    List<Class<?>> returnTypes;

    public CommonMethod(CommonElement<T> element,
                        CommonClass<T> defaultClass, Method method) {
        this.element = element;
        this.defaultClass = defaultClass;
        this.method = method;
        this.element.initializeConfigListener(this::reload);
        initializeReturnTypes();
    }

    private void initializeReturnTypes() {
        Class<?> returnType = method.getReturnType();
        returnFuture = checkReturnFuture(returnType);
        returnCollection = Collection.class.isAssignableFrom(returnType);
        returnMap = Map.class.isAssignableFrom(returnType);
        returnPair = Pair.class.isAssignableFrom(returnType);
        returnTriple = Triple.class.isAssignableFrom(returnType);

        val genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            // 错误的泛型时
            checkUnParameterizedType(genericReturnType, returnFuture);
            return;
        }

        // 方法返回泛型时
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (returnFuture) {
            // 返回Future类型, 则多处理一层泛型
            val futureTypeArgument = actualTypeArguments[0];
            if (futureTypeArgument instanceof Class) {
                returnType = (Class<?>) futureTypeArgument;
                returnCollection = Collection.class.isAssignableFrom(returnType);
                returnMap = Map.class.isAssignableFrom(returnType);
                returnPair = Pair.class.isAssignableFrom(returnType);
                returnTriple = Triple.class.isAssignableFrom(returnType);
            }
            if (!(futureTypeArgument instanceof ParameterizedType)) {
                // 错误的泛型时
                checkUnParameterizedType(futureTypeArgument, false);
                return;
            }
            parameterizedType = (ParameterizedType) futureTypeArgument;
            returnType = (Class<?>) parameterizedType.getRawType();
            returnCollection = Collection.class.isAssignableFrom(returnType);
            returnMap = Map.class.isAssignableFrom(returnType);
            returnPair = Pair.class.isAssignableFrom(returnType);
            returnTriple = Triple.class.isAssignableFrom(returnType);
            actualTypeArguments = parameterizedType.getActualTypeArguments();
        }

        if (returnCollection || returnPair || returnTriple) {
            // 以泛型参数类型作为返回值解析目标类型
            returnTypes = processActualTypeArguments(actualTypeArguments);
        } else {
            // 以泛型类型作为返回值解析目标类型
            returnTypes = newArrayList(returnType);
        }
    }

    protected boolean checkReturnFuture(Class<?> returnType) {
        returnJavaFuture = Future.class == returnType;
        returnReactorMono = ReactorCheckHelper.checkReturnReactorMono(returnType);
        returnRxJavaSingle = RxJavaCheckHelper.checkReturnRxJavaSingle(returnType);
        returnRxJava2Single = RxJavaCheckHelper.checkReturnRxJava2Single(returnType);
        returnRxJava3Single = RxJavaCheckHelper.checkReturnRxJava3Single(returnType);
        returnMutinyUni = MutinyCheckHelper.checkReturnMutinyUni(returnType);
        return returnJavaFuture || returnReactorMono
                || returnRxJavaSingle || returnRxJava2Single || returnRxJava3Single
                || returnMutinyUni;
    }

    private void checkUnParameterizedType(Type type, boolean checkReturnFutureOrNot) {
        if (checkReturnFutureOrNot || returnCollection || returnPair || returnTriple) {
            // 如返回支持的泛型类型则抛出异常
            // 不包括Map<K, V>
            throw new IllegalStateException(RETURN_GENERIC_ERROR);
        } else if (type instanceof TypeVariable) {
            // 返回类型变量指定的类型时
            // 检查是否为<T extend CncResponse>类型
            checkTypeVariableBounds(type);
            returnTypes = newArrayList(CncResponse.class);
        } else {
            // 否则以方法返回类型作为实际返回类型
            // 返回Map时, 可直接解析返回值为Map
            returnTypes = newArrayList((Class<?>) type);
        }
    }

    private void checkTypeVariableBounds(Type type) {
        val bounds = ((TypeVariable<?>) type).getBounds();
        if (bounds.length != 1 || !CncResponse.class
                .isAssignableFrom((Class<?>) bounds[0])) {
            throw new IllegalStateException(RETURN_GENERIC_ERROR);
        }
    }

    private List<Class<?>> processActualTypeArguments(Type[] actualTypeArguments) {
        List<Class<?>> result = newArrayList();
        for (Type actualTypeArgument : actualTypeArguments) {
            if (actualTypeArgument instanceof TypeVariable) {
                checkTypeVariableBounds(actualTypeArgument);
                result.add(CncResponse.class);
                continue;
            }
            result.add((Class<?>) actualTypeArgument);
        }
        return result;
    }

    protected void initialize() {
        element.initializeConfigurer(method);
        element.setUpBeforeInitialization(defaultClass.clazz, method, defaultClass.element);
        requestUrls = buildRequestUrls();
        element.initialize(method, defaultClass.element.base);
        element.tearDownAfterInitialization(defaultClass.clazz, method, defaultClass.element);
    }

    private List<String> buildRequestUrls() {
        val urls = emptyThen(element.buildMappingUrls(method), () ->
                newArrayList(defaultClass.mappingMethodNameDisabled ? "" : "/" + method.getName()));
        List<String> concatedUrls = newArrayList();
        for (val url : urls) {
            if (isBlank(url)) {
                concatedUrls.addAll(defaultClass.baseUrls);
            } else {
                concatedUrls.addAll(defaultClass.baseUrls.stream()
                        .map(base -> checkBlank(base, () -> url, b -> b + url)).toList());
            }
        }
        return concatedUrls;
    }

    @Override
    public void reload() {
        synchronized (element.configLock) {
            initialize();
        }
    }

    public abstract Object execute(Object[] args);
}
