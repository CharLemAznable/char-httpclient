package com.github.charlemaznable.httpclient.ws.soap;

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
@XmlRootElement(name = "Envelope", namespace = NameSpace.SOAP_URI)
public final class ResponseEntity implements Entity<ResponseEntity> {

    @XmlElement(name = "Body", namespace = NameSpace.SOAP_URI)
    private Body body;
}
