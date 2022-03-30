package com.github.charlemaznable.httpclient.ws.common;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class NameSpace {

    public static final String SOAP_PREFIX = "soap";
    public static final String SOAP_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP_12_URI = "http://www.w3.org/2003/05/soap-envelope";

    public static final String XSI_PREFIX = "xsi";
    public static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String XSD_PREFIX = "xsd";
    public static final String XSD_URI = "http://www.w3.org/2001/XMLSchema";
}
