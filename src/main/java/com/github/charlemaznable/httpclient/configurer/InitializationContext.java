package com.github.charlemaznable.httpclient.configurer;

import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class InitializationContext {

    private static final ThreadLocal<Class<?>> initializingClassLocal = new InheritableThreadLocal<>();
    private static final ThreadLocal<Method> initializingMethodLocal = new InheritableThreadLocal<>();

    public static void setInitializingClass(Class<?> ohClass) {
        initializingClassLocal.set(ohClass);
    }

    public static void setInitializingMethod(Method ohMethod) {
        initializingMethodLocal.set(ohMethod);
    }

    public static Class<?> getInitializingClass() {
        return initializingClassLocal.get();
    }

    public static Method getInitializingMethod() {
        return initializingMethodLocal.get();
    }

    public static void clearInitializingClass() {
        initializingClassLocal.remove();
    }

    public static void clearInitializingMethod() {
        initializingMethodLocal.remove();
    }
}
