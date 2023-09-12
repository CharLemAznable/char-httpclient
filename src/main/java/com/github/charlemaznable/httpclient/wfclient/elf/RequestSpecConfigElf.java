package com.github.charlemaznable.httpclient.wfclient.elf;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class RequestSpecConfigElf {

    public static final String REQUEST_BODY_AS_STRING = "RequestBodyAsStringSpecAttributeKey";

    private static final CopyOnWriteArrayList<RequestSpecConfigurer> configurers;

    static {
        configurers = StreamSupport
                .stream(ServiceLoader.load(RequestSpecConfigurer.class).spliterator(), false)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public static void configRequestSpec(WebClient.RequestBodyUriSpec requestSpec,
                                         CommonExecute<?, ?, ?, ?> execute) {
        for (val configurer : configurers) {
            configurer.configRequestSpec(requestSpec, execute);
        }
    }
}
