package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.Mapping;
import com.github.charlemaznable.httpclient.common.CommonReturnListTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import lombok.SneakyThrows;
import lombok.val;
import okio.BufferedSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnListTest extends CommonReturnListTest {

    @SneakyThrows
    @Test
    public void testList() {
        startMockWebServer();

        val ohLoader = OhFactory.ohLoader(reflectFactory());

        val httpClient = ohLoader.getClient(ListHttpClient.class);

        List<Bean> beans = httpClient.sampleListBean();
        Bean bean1 = beans.get(0);
        Bean bean2 = beans.get(1);
        assertEquals("John", bean1.getName());
        assertEquals("Doe", bean2.getName());
        val futureBeans = httpClient.sampleFutureListBean();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureBeans::isDone);
        beans = futureBeans.get();
        bean1 = beans.get(0);
        bean2 = beans.get(1);
        assertEquals("John", bean1.getName());
        assertEquals("Doe", bean2.getName());

        List<String> strs = httpClient.sampleListString();
        String str1 = strs.get(0);
        String str2 = strs.get(1);
        assertEquals("John", str1);
        assertEquals("Doe", str2);
        val futureStrs = httpClient.sampleFutureListString();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureStrs::isDone);
        strs = futureStrs.get();
        str1 = strs.get(0);
        str2 = strs.get(1);
        assertEquals("John", str1);
        assertEquals("Doe", str2);

        val bufferedSources = httpClient.sampleListBufferedSource();
        assertEquals(1, bufferedSources.size());
        assertEquals(HttpStatus.OK.getReasonPhrase(), bufferedSources.get(0).readUtf8());

        shutdownMockWebServer();
    }

    @OhClient
    @Mapping("${root}:41192")
    public interface ListHttpClient {

        List<Bean> sampleListBean();

        Future<List<Bean>> sampleFutureListBean();

        List<String> sampleListString();

        Future<List<String>> sampleFutureListString();

        List<BufferedSource> sampleListBufferedSource();
    }
}
