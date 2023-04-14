package com.github.charlemaznable.httpclient.ohclient.elf;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import lombok.NoArgsConstructor;
import lombok.val;
import okhttp3.Request;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class RequestBuilderConfigElf {

    private static final ServiceLoader<RequestBuilderConfigurer> configurers;

    static {
        configurers = ServiceLoader.load(RequestBuilderConfigurer.class);
    }

    public static void configRequestBuilder(Request.Builder requestBuilder,
                                            CommonExecute<?, ?, ?> execute) {
        for (val configurer : configurers) {
            configurer.configRequestBuilder(requestBuilder, execute);
        }
    }
}
