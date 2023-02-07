package com.github.charlemaznable.httpclient.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.httpclient.configurer.AcceptCharsetConfigurer;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface AcceptCharsetConfig extends AcceptCharsetConfigurer {

    @Config("acceptCharset")
    String acceptCharsetString();

    @Override
    default Charset acceptCharset() {
        try {
            return notNullThen(acceptCharsetString(), Charset::forName);
        } catch (Exception e) {
            return null;
        }
    }
}
