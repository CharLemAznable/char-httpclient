package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.ws.WsVxClient12;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;

import java.util.concurrent.CompletableFuture;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;

@WsVxClient12("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx")
@DefaultFallbackDisabled
public interface WeatherVxClient {

    CompletableFuture<GetSupportProvince.Response> getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    CompletableFuture<GetSupportCity.Response> getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
