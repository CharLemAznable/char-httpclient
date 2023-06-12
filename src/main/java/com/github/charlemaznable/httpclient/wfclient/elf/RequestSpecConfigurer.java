package com.github.charlemaznable.httpclient.wfclient.elf;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import org.springframework.web.reactive.function.client.WebClient;

public interface RequestSpecConfigurer {

    void configRequestSpec(WebClient.RequestBodyUriSpec requestSpec,
                           CommonExecute<?, ?, ?> execute);
}
