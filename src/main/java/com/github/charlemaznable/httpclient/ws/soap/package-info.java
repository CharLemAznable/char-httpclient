@XmlSchema(xmlns = {
        @XmlNs(prefix = NameSpace.SOAP_PREFIX, namespaceURI = NameSpace.SOAP_URI),
        @XmlNs(prefix = NameSpace.XSI_PREFIX, namespaceURI = NameSpace.XSI_URI),
        @XmlNs(prefix = NameSpace.XSD_PREFIX, namespaceURI = NameSpace.XSD_URI),
})
package com.github.charlemaznable.httpclient.ws.soap;

import com.github.charlemaznable.httpclient.ws.common.NameSpace;
import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlSchema;