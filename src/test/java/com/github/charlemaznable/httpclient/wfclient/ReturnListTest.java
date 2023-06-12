package com.github.charlemaznable.httpclient.wfclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnListTest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnListTest extends CommonReturnListTest {

    @SneakyThrows
    @Test
    public void testList() {
        startMockWebServer();

        val wfLoader = WfFactory.wfLoader(reflectFactory());

        val httpClient = wfLoader.getClient(ListHttpClient.class);

        List<Bean> beans = requireNonNull(httpClient.sampleListBean().block());
        Bean bean1 = beans.get(0);
        Bean bean2 = beans.get(1);
        assertEquals("John", bean1.getName());
        assertEquals("Doe", bean2.getName());

        List<String> strs = requireNonNull(httpClient.sampleListString().block());
        String str1 = strs.get(0);
        String str2 = strs.get(1);
        assertEquals("John", str1);
        assertEquals("Doe", str2);

        shutdownMockWebServer();
    }

    @WfClient
    @Mapping("${root}:41192")
    public interface ListHttpClient {

        Mono<List<Bean>> sampleListBean();

        Mono<List<String>> sampleListString();
    }
}
