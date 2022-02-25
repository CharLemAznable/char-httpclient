package com.github.charlemaznable.httpclient.common;

public interface CncRequest<T extends CncResponse> {

    Class<? extends T> responseClass();
}
