package com.github.charlemaznable.httpclient.common;

import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.httpclient.annotation.MappingMethodNameDisabled;
import com.github.charlemaznable.httpclient.configurer.MappingMethodNameDisabledConfigurer;
import com.github.charlemaznable.httpclient.resilience.common.ResilienceMeterBinder;
import com.google.common.cache.LoadingCache;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.google.common.cache.CacheLoader.from;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@Accessors(fluent = true)
public abstract class CommonClass<T extends CommonBase<T>>
        implements Reloadable, ResilienceMeterBinder {

    @Getter
    final CommonElement<T> element;
    final T defaultBase;
    @Getter
    final Class<?> clazz;
    @Getter
    final Logger logger;

    List<String> baseUrls;
    boolean mappingMethodNameDisabled;

    final LoadingCache<Method, CommonMethod<T>>
            commonMethodCache = simpleCache(from(this::loadMethod));

    public CommonClass(CommonElement<T> element,
                       T defaultBase, Class<?> clazz) {
        this.element = element;
        this.defaultBase = defaultBase;
        this.clazz = clazz;
        this.logger = LoggerFactory.getLogger(clazz);
        this.element.initializeConfigListener(this::reload);
    }

    protected void initialize() {
        element.initializeConfigurer(clazz);
        element.setUpBeforeInitialization(clazz, null, null);
        baseUrls = emptyThen(element.buildMappingUrls(clazz), () -> newArrayList(""));
        mappingMethodNameDisabled = checkMappingMethodNameDisabled();
        element.initialize(clazz, defaultBase);
        element.tearDownAfterInitialization(clazz, null, null);
    }

    private boolean checkMappingMethodNameDisabled() {
        if (element.configurer instanceof MappingMethodNameDisabledConfigurer disabledConfigurer)
            return disabledConfigurer.disabledMappingMethodName();
        return isAnnotated(clazz, MappingMethodNameDisabled.class);
    }

    @Override
    public void reload() {
        synchronized (element.configLock) {
            initialize();
            commonMethodCache.invalidateAll();
        }
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        synchronized (element.configLock) {
            element.bindTo(registry);
            commonMethodCache.asMap().values().forEach(
                    method -> method.element.bindTo(registry));
        }
    }

    protected abstract CommonMethod<T> loadMethod(Method method);

    protected Object execute(Method method, Object[] args) throws Exception {
        if (method.getDeclaringClass().equals(Reloadable.class) ||
                method.getDeclaringClass().equals(ResilienceMeterBinder.class)) {
            return method.invoke(this, args);
        }
        return get(commonMethodCache, method).execute(args);
    }
}
