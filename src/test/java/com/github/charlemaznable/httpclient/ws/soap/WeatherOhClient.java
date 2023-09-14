package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.FixedHeader;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.ws.WsOhClient;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;

import java.util.concurrent.CompletableFuture;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;
import static com.github.charlemaznable.httpclient.ws.common.Constants.SOAP_ACTION_KEY;

@WsOhClient("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx")
@DefaultFallbackDisabled
public interface WeatherOhClient {

    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportProvince.SOAP_ACTION)
    CompletableFuture<GetSupportProvince.Response> getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportCity.SOAP_ACTION)
    CompletableFuture<GetSupportCity.Response> getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
