package com.github.charlemaznable.httpclient.configurer;

import java.lang.reflect.Method;

public interface InitializationConfigurer extends Configurer {

    InitializationConfigurer INSTANCE = new InitializationConfigurer() {};

    default void setUpBeforeInitialization(Class<?> ohClass, Method ohMethod) {
        InitializationContext.setOhClass(ohClass);
        InitializationContext.setOhMethod(ohMethod);
    }

    default void tearDownAfterInitialization(Class<?> ohClass, Method ohMethod) {
        InitializationContext.clearOhClass();
        InitializationContext.clearOhMethod();
    }
}
