package com.github.charlemaznable.httpclient.ohclient.elf;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import lombok.NoArgsConstructor;
import lombok.val;
import okhttp3.Request;

import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class RequestBuilderConfigElf {

    private static final CopyOnWriteArrayList<RequestBuilderConfigurer> configurers;

    static {
        configurers = StreamSupport
                .stream(ServiceLoader.load(RequestBuilderConfigurer.class).spliterator(), false)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public static void configRequestBuilder(Request.Builder requestBuilder,
                                            CommonExecute<?, ?, ?, ?> execute) {
        for (val configurer : configurers) {
            configurer.configRequestBuilder(requestBuilder, execute);
        }
    }
}
