package com.github.charlemaznable.httpclient.ws.soap12;

import com.github.charlemaznable.httpclient.ws.common.Entity;
import com.github.charlemaznable.httpclient.ws.common.NameSpace;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Envelope", namespace = NameSpace.SOAP_12_URI)
public final class RequestEntity implements Entity<RequestEntity> {

    @XmlElement(name = "Header", namespace = NameSpace.SOAP_12_URI)
    private Header header = new Header();

    @XmlElement(name = "Body", namespace = NameSpace.SOAP_12_URI)
    private Body body;
}
