package com.github.charlemaznable.httpclient.configurer;

import java.lang.reflect.Method;

public interface InitializationConfigurer extends Configurer {

    InitializationConfigurer INSTANCE = new InitializationConfigurer() {};

    default void setUpBeforeInitialization(Class<?> clazz, Method method) {
        InitializationContext.setInitializingClass(clazz);
        InitializationContext.setInitializingMethod(method);
    }

    default void tearDownAfterInitialization(Class<?> clazz, Method method) {
        InitializationContext.clearInitializingClass();
        InitializationContext.clearInitializingMethod();
    }
}
