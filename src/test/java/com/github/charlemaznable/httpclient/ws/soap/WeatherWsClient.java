package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.common.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.common.FixedHeader;
import com.github.charlemaznable.httpclient.common.Parameter;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientLoggingLevel;
import com.github.charlemaznable.httpclient.ws.WsOhClient;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;
import static com.github.charlemaznable.httpclient.ws.common.Constants.SOAP_ACTION_KEY;

@WsOhClient("http://ws.webxml.com.cn/WebServices/WeatherWebService.asmx")
@DefaultFallbackDisabled
@ClientLoggingLevel(Level.BODY)
public interface WeatherWsClient {

    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportProvince.SOAP_ACTION)
    GetSupportProvince.Response getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    @FixedHeader(name = SOAP_ACTION_KEY, value = GetSupportCity.SOAP_ACTION)
    GetSupportCity.Response getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
