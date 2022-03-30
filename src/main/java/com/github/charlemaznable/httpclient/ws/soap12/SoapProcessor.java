package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.httpclient.common.ResponseParse.ResponseParser;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.github.charlemaznable.httpclient.ws.common.Constants.CONTENT_KEY;
import static com.google.common.net.MediaType.SOAP_XML_UTF_8;

public final class SoapProcessor implements ContentFormatter, ResponseParser {

    @Override
    public String contentType() {
        return SOAP_XML_UTF_8.toString();
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
