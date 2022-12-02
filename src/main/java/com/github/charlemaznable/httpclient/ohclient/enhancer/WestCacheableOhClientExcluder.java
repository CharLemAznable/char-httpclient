package com.github.charlemaznable.httpclient.ohclient.enhancer;

import com.github.bingoohuang.westcache.spring.exclude.WestCacheExcludeAnnotationTypeSupplier;
import com.github.charlemaznable.httpclient.ohclient.OhClient;
import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

@AutoService(WestCacheExcludeAnnotationTypeSupplier.class)
public class WestCacheableOhClientExcluder implements WestCacheExcludeAnnotationTypeSupplier {

    @Override
    public List<Class<? extends Annotation>> get() {
        return newArrayList(OhClient.class);
    }
}
