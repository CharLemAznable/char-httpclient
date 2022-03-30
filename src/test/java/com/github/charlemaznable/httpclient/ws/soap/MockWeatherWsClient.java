package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.common.FixedHeader;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.Parameter;
import com.github.charlemaznable.httpclient.ws.WsOhClient;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;
import static com.github.charlemaznable.httpclient.ws.common.Constants.SOAP_ACTION_KEY;

@WsOhClient
@Mapping("${root}:41240/ws")
public interface MockWeatherWsClient {

    @Mapping
    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportProvince.SOAP_ACTION)
    GetSupportProvince.Response getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    @Mapping
    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportCity.SOAP_ACTION)
    GetSupportCity.Response getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
