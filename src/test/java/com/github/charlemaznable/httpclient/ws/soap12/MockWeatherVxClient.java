package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.ws.WsVxClient12;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import io.vertx.core.Future;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;

@WsVxClient12("${root}:41241/ws")
public interface MockWeatherVxClient {

    Future<GetSupportProvince.Response> getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    Future<GetSupportCity.Response> getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
