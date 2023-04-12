package com.github.charlemaznable.httpclient.ohclient.elf;

import okhttp3.OkHttpClient;

public interface GlobalClientSupplier {

    OkHttpClient supply();
}
