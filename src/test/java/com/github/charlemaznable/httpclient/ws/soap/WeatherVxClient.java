package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.annotation.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.annotation.FixedHeader;
import com.github.charlemaznable.httpclient.annotation.Parameter;
import com.github.charlemaznable.httpclient.ws.WsVxClient;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import io.vertx.core.Future;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;
import static com.github.charlemaznable.httpclient.ws.common.Constants.SOAP_ACTION_KEY;

@WsVxClient("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx")
@DefaultFallbackDisabled
public interface WeatherVxClient {

    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportProvince.SOAP_ACTION)
    Future<GetSupportProvince.Response> getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportCity.SOAP_ACTION)
    Future<GetSupportCity.Response> getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
