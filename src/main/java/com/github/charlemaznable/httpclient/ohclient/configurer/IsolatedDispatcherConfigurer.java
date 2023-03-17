package com.github.charlemaznable.httpclient.ohclient.configurer;

import com.github.charlemaznable.httpclient.configurer.Configurer;
import okhttp3.Dispatcher;

public interface IsolatedDispatcherConfigurer extends Configurer {

    default boolean isolatedDispatcher() {
        return true;
    }

    default Dispatcher customDispatcher() {
        return null;
    }
}
