package com.github.charlemaznable.httpclient.wfclient.spring;

import com.github.charlemaznable.httpclient.common.spring.CommonSpringNakedConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Import;

import static com.github.charlemaznable.httpclient.wfclient.WfFactory.springWfLoader;
import static org.joor.Reflect.on;

@Import(CommonSpringNakedConfiguration.class)
public class WfSpringNakedConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(springWfLoader()).field("wfCache").call("invalidateAll");
    }
}
