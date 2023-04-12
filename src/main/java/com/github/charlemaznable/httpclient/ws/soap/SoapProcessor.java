package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.annotation.ContentFormat;
import com.github.charlemaznable.httpclient.annotation.ResponseParse;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;
import static com.google.common.net.MediaType.XML_UTF_8;

public final class SoapProcessor implements ContentFormat.ContentFormatter, ResponseParse.ResponseParser {

    @Override
    public String contentType() {
        return XML_UTF_8.toString();
    }

    @Override
    public String format(@Nonnull Map<String, Object> parameterMap,
                         @Nonnull Map<String, Object> contextMap) {
        return new RequestEntity().withContent(parameterMap.get(CONTENT_KEY)).toXml();
    }

    @Override
    public Object parse(@Nonnull String responseContent,
                        @Nonnull Class<?> returnType,
                        @Nonnull Map<String, Object> contextMap) {
        return new ResponseEntity().fromXml(responseContent, returnType).content();
    }
}
