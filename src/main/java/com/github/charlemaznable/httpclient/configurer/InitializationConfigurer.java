package com.github.charlemaznable.httpclient.configurer;

import java.lang.reflect.Method;

public interface InitializationConfigurer extends Configurer {

    InitializationConfigurer INSTANCE = new InitializationConfigurer() {};

    ThreadLocal<Class<?>> ohClassLocal = new InheritableThreadLocal<>();
    ThreadLocal<Method> ohMethodLocal = new InheritableThreadLocal<>();

    default void setUpBeforeInitialization(Class<?> ohClass, Method ohMethod) {
        ohClassLocal.set(ohClass);
        ohMethodLocal.set(ohMethod);
    }

    default void tearDownAfterInitialization(Class<?> ohClass, Method ohMethod) {
        ohClassLocal.remove();
        ohMethodLocal.remove();
    }
}
