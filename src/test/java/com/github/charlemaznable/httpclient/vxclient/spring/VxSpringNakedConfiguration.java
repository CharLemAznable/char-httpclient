package com.github.charlemaznable.httpclient.vxclient.spring;

import com.github.charlemaznable.core.vertx.spring.VertxImport;
import com.github.charlemaznable.httpclient.common.spring.CommonSpringNakedConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.vxclient.VxFactory.springVxLoader;
import static org.joor.Reflect.on;

@VertxImport
@Import(CommonSpringNakedConfiguration.class)
public class VxSpringNakedConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(springVxLoader()).field("vxCache").call("invalidateAll");
    }
}
