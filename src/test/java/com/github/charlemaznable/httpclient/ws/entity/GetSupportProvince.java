package com.github.charlemaznable.httpclient.ws.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class GetSupportProvince {

    public static final String SOAP_ACTION = "http://WebXml.com.cn/getSupportProvince";

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "getSupportProvince", namespace = WebNameSpace.WEB_URI)
    @XmlType(name = "getSupportProvince", namespace = WebNameSpace.WEB_URI) // used in unmarshal type matching
    public static final class Request {
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "getSupportProvinceResponse", namespace = WebNameSpace.WEB_URI)
    public static final class Response {

        @XmlElementWrapper(name = "getSupportProvinceResult", namespace = WebNameSpace.WEB_URI)
        @XmlElement(name = "string", namespace = WebNameSpace.WEB_URI)
        private List<String> result;
    }
}
