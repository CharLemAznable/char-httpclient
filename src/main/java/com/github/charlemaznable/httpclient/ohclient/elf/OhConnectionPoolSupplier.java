package com.github.charlemaznable.httpclient.ohclient.elf;

import okhttp3.ConnectionPool;

public interface OhConnectionPoolSupplier {

    ConnectionPool supply();
}
