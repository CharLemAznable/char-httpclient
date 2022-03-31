package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.common.Parameter;
import com.github.charlemaznable.httpclient.ws.WsOhClient12;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;

@WsOhClient12("${root}:41241/ws")
public interface MockWeatherWsClient {

    GetSupportProvince.Response getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    GetSupportCity.Response getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
