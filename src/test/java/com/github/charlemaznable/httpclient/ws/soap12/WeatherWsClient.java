package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.common.DefaultFallbackDisabled;
import com.github.charlemaznable.httpclient.common.Mapping;
import com.github.charlemaznable.httpclient.common.Parameter;
import com.github.charlemaznable.httpclient.ohclient.annotation.ClientLoggingLevel;
import com.github.charlemaznable.httpclient.ws.WsOhClient12;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportCity;
import com.github.charlemaznable.httpclient.ws.entity.GetSupportProvince;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;

@WsOhClient12
@Mapping("http://ws.webxml.com.cn/WebServices/WeatherWebService.asmx")
@DefaultFallbackDisabled
@ClientLoggingLevel(Level.BODY)
public interface WeatherWsClient {

    @Mapping
    GetSupportProvince.Response getSupportProvince(
            @Parameter(CONTENT_KEY) GetSupportProvince.Request request);

    @Mapping
    GetSupportCity.Response getSupportCity(
            @Parameter(CONTENT_KEY) GetSupportCity.Request request);
}
