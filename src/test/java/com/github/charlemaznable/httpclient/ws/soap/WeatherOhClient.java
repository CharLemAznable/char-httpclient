package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.FixedHeader;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceFallback;
import com.github.charlemaznable.httpclient.resilience.annotation.ResilienceTimeLimiter;
import com.github.charlemaznable.httpclient.ws.WsOhClient;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import com.github.charlemaznable.httpclient.ws.entity.TestFallback;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;
import static com.github.charlemaznable.httpclient.ws.common.Constants.SOAP_ACTION_KEY;

@WsOhClient("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx")
@DefaultFallbackDisabled
@ResilienceTimeLimiter(timeoutDurationInMillis = 15_000L)
@ResilienceFallback(TestFallback.class)
public interface WeatherOhClient {

    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportProvince.SOAP_ACTION)
    GetSupportProvince.Response getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportCity.SOAP_ACTION)
    GetSupportCity.Response getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
