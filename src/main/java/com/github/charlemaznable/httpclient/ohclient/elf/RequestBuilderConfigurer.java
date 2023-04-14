package com.github.charlemaznable.httpclient.ohclient.elf;

import com.github.charlemaznable.httpclient.common.CommonExecute;
import okhttp3.Request;

public interface RequestBuilderConfigurer {

    void configRequestBuilder(Request.Builder requestBuilder,
                              CommonExecute<?, ?, ?> execute);
}
