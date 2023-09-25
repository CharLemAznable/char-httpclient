package com.github.charlemaznable.httpclient.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class CommonResponse {

    private final int statusCode;
    private final HttpHeaders headers;
    private final String body;
}
