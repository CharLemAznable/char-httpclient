package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.ws.common.Entity;
import com.github.charlemaznable.httpclient.ws.common.NameSpace;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("rawtypes")
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Envelope", namespace = NameSpace.SOAP_URI)
public final class RequestEntity implements Entity<RequestEntity> {

    @XmlElement(name = "Header", namespace = NameSpace.SOAP_URI)
    private Header header = new Header();

    @XmlElement(name = "Body", namespace = NameSpace.SOAP_URI)
    private Body body;
}
