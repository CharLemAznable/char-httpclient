package com.github.charlemaznable.httpclient.ohclient.elf;

import okhttp3.Dispatcher;

public interface OhDispatcherSupplier {

    Dispatcher supply();
}
