package com.github.charlemaznable.httpclient.wfclient.elf;

import org.springframework.web.reactive.function.client.WebClient;

public interface GlobalClientSupplier {

    WebClient supply();
}
