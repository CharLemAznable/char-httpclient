package com.github.charlemaznable.httpclient.configurer;

import java.nio.charset.Charset;

public interface AcceptCharsetConfigurer extends Configurer {

    Charset acceptCharset();
}
