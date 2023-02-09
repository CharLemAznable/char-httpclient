package com.github.charlemaznable.httpclient.configurer;

import java.util.List;

public interface MappingConfigurer extends Configurer {

    List<String> urls();
}
