package com.github.charlemaznable.httpclient.ws.common;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;

import java.io.StringReader;
import java.io.StringWriter;

@SuppressWarnings("rawtypes")
public interface Entity<E extends Entity> {

    Body getBody();

    E setBody(Body body);

    default <T> E withContent(T content) {
        return setBody(new Body<T>().setContent(content));
    }

    @SuppressWarnings("unchecked")
    default <T> T content() {
        return (T) getBody().getContent();
    }

    @SneakyThrows
    default String toXml() {
        val context = JAXBContext.newInstance(this.getClass(),
                this.getBody().getContent().getClass());
        val marshaller = context.createMarshaller();
        val writer = new StringWriter();
        marshaller.marshal(this, writer);
        return writer.toString();
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    default <T> E fromXml(String xml, Class<T> clazz) {
        val context = JAXBContext.newInstance(this.getClass(), clazz);
        val unmarshaller = context.createUnmarshaller();
        return (E) unmarshaller.unmarshal(new StringReader(xml));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    class Header {}

    @Getter
    @Setter
    @Accessors(chain = true)
    @XmlAccessorType(XmlAccessType.FIELD)
    class Body<T> {

        @XmlAnyElement(lax = true)
        private T content;
    }
}
