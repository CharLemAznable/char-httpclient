package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceFallback;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceTimeLimiter;
import com.github.charlemaznable.httpclient.ws.WsVxClient12;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import com.github.charlemaznable.httpclient.ws.entity.TestFallback;
import io.vertx.core.Future;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;

@WsVxClient12("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx")
@DefaultFallbackDisabled
@ResilienceTimeLimiter(timeoutDurationInMillis = 15_000L)
@ResilienceFallback(TestFallback.class)
public interface WeatherVxClient {

    Future<GetSupportProvince.Response> getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    Future<GetSupportCity.Response> getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
