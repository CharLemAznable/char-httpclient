package com.github.charlemaznable.httpclient.configurer;

import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class InitializationContext {

    private static final ThreadLocal<Class<?>> ohClassLocal = new InheritableThreadLocal<>();
    private static final ThreadLocal<Method> ohMethodLocal = new InheritableThreadLocal<>();

    public static void setOhClass(Class<?> ohClass) {
        ohClassLocal.set(ohClass);
    }

    public static void setOhMethod(Method ohMethod) {
        ohMethodLocal.set(ohMethod);
    }

    public static Class<?> getOhClass() {
        return ohClassLocal.get();
    }

    public static Method getOhMethod() {
        return ohMethodLocal.get();
    }

    public static void clearOhClass() {
        ohClassLocal.remove();
    }

    public static void clearOhMethod() {
        ohMethodLocal.remove();
    }
}
