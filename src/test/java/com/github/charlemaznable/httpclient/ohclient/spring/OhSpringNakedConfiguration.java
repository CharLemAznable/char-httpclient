package com.github.charlemaznable.httpclient.ohclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringNakedConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.ohclient.OhFactory.springOhLoader;
import static org.joor.Reflect.on;

@Import(CommonSpringNakedConfiguration.class)
public class OhSpringNakedConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(springOhLoader()).field("ohCache").call("invalidateAll");
    }
}
