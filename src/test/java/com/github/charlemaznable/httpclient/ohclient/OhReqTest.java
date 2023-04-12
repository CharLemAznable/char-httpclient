package com.github.charlemaznable.httpclient.ohclient;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.common.CommonFallbackTest;
import com.github.charlemaznable.httpclient.common.CommonReqTest;
import com.github.charlemaznable.httpclient.common.HttpStatus;
import com.github.charlemaznable.httpclient.common.StatusError;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class
OhReqTest extends CommonReqTest {

    @SneakyThrows
    @Test
    public void testOhReq() {
        startMockWebServer(41103);

        val ohReq1 = new OhReq("http://127.0.0.1:41103/sample1")
                .acceptCharset(ISO_8859_1)
                .contentFormat(new ContentFormat.FormContentFormatter())
                .header("AAA", "aaa")
                .headers(of("AAA", null, "BBB", "bbb"))
                .parameter("CCC", "ccc");
        assertEquals("Sample1", ohReq1.get());

        val ohReq2 = new OhReq()
                .req("http://127.0.0.1:41103/sample2")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"));
        assertEquals("Sample2", ohReq2.post());

        val ohReq3 = new OhReq("http://127.0.0.1:41103")
                .req("/sample3?DDD=ddd")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc");
        val future3 = ohReq3.getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future3::isDone);
        assertEquals("Sample3", future3.get());

        val ohReq4 = new OhReq("http://127.0.0.1:41103")
                .req("/sample4")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc");
        val future4 = ohReq4.postFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future4::isDone);
        assertEquals("Sample4", future4.get());

        val ohReq5 = new OhReq("http://127.0.0.1:41103/sample5");
        try {
            ohReq5.get();
        } catch (StatusError e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        Future<String> future5 = ohReq5.getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future5::isDone);
        try {
            future5.get();
        } catch (ExecutionException ex) {
            val e = (StatusError) ex.getCause();
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        try {
            ohReq5.parameter("AAA", "aaa").get();
        } catch (StatusError e) {
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
        }
        future5 = ohReq5.parameter("AAA", "aaa").getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future5::isDone);
        try {
            future5.get();
        } catch (ExecutionException ex) {
            val e = (StatusError) ex.getCause();
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
        }

        val ohReq6 = new OhReq("http://127.0.0.1:41103/sample6")
                .statusFallback(HttpStatus.NOT_FOUND, new CommonFallbackTest.NotFound())
                .statusSeriesFallback(HttpStatus.Series.CLIENT_ERROR, new CommonFallbackTest.ClientError());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), ohReq6.get());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), ohReq6.parameter("AAA", "aaa").get());

        val ohReq7 = new OhReq("http://127.0.0.1:41103/sample7")
                .contentFormat(new ContentFormat.JsonContentFormatter())
                .parameter("BBB", "bbb")
                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa");
        assertEquals("Sample7", ohReq7.get());
        assertEquals("Sample7", ohReq7.post());

        try {
            new OhReq("http://127.0.0.1:51103/sample").get();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConnectException);
        }
        try {
            val errorFuture = new OhReq("http://127.0.0.1:51103/sample").getFuture();
            await().forever().pollDelay(Duration.ofMillis(100)).until(errorFuture::isDone);
            errorFuture.get();
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof ConnectException);
        }

        val instance = new OhReq("http://127.0.0.1:41103").buildInstance();

        val insReq1 = instance.req("/sample1")
                .acceptCharset(ISO_8859_1)
                .contentFormat(new ContentFormat.FormContentFormatter())
                .header("AAA", "aaa")
                .headers(of("AAA", null, "BBB", "bbb"))
                .parameter("CCC", "ccc");
        assertEquals("Sample1", insReq1.get());

        val insReq2 = instance.req("/sample2")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"));
        assertEquals("Sample2", insReq2.post());

        val insReq3 = instance.req("/sample3?DDD=ddd")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc");
        val insFuture3 = insReq3.getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(insFuture3::isDone);
        assertEquals("Sample3", insFuture3.get());

        val insReq4 = instance.req("/sample4")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc");
        val insFuture4 = insReq4.postFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(insFuture4::isDone);
        assertEquals("Sample4", insFuture4.get());

        val insReq5 = instance.req("/sample5");
        try {
            insReq5.get();
        } catch (StatusError e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        Future<String> insFuture5 = insReq5.getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(insFuture5::isDone);
        try {
            insFuture5.get();
        } catch (ExecutionException ex) {
            val e = (StatusError) ex.getCause();
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        try {
            insReq5.parameter("AAA", "aaa").get();
        } catch (StatusError e) {
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
        }
        insFuture5 = insReq5.parameter("AAA", "aaa").getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(insFuture5::isDone);
        try {
            insFuture5.get();
        } catch (ExecutionException ex) {
            val e = (StatusError) ex.getCause();
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
        }

        val insReq6 = instance.req("/sample6")
                .statusFallback(HttpStatus.NOT_FOUND, new CommonFallbackTest.NotFound())
                .statusSeriesFallback(HttpStatus.Series.CLIENT_ERROR, new CommonFallbackTest.ClientError());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), insReq6.get());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), insReq6.parameter("AAA", "aaa").get());

        val insReq7 = instance.req("/sample7")
                .contentFormat(new ContentFormat.JsonContentFormatter())
                .parameter("BBB", "bbb")
                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa");
        assertEquals("Sample7", insReq7.get());
        assertEquals("Sample7", insReq7.post());

        shutdownMockWebServer();
    }
}
