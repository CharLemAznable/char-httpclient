package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.AcceptCharsetConfigurer;

import java.nio.charset.Charset;

import static com.github.charlemaznable.httpclient.configurer.configservice.ConfigurerElf.parseStringToValue;

public interface AcceptCharsetConfig extends AcceptCharsetConfigurer {

    @Config("acceptCharset")
    String acceptCharsetString();

    @Override
    default Charset acceptCharset() {
        return parseStringToValue(acceptCharsetString(), null, Charset::forName);
    }
}
