package com.github.charlemaznable.httpclient.common;

import lombok.experimental.Delegate;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.github.charlemaznable.core.lang.Mapp.newTreeMap;
import static java.util.Objects.isNull;

public final class HttpHeaders {

    @Delegate
    private final Map<String, List<String>> headers;

    public HttpHeaders(Map<String, List<String>> headers) {
        this.headers = Collections.unmodifiableMap(
                newTreeMap(headers, String.CASE_INSENSITIVE_ORDER));
    }

    public HttpHeaders(Iterable<Map.Entry<String, String>> headers) {
        val headersMap = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        headers.forEach(entry -> {
            List<String> values = headersMap.get(entry.getKey());
            if (isNull(values)) {
                values = new ArrayList<>();
                headersMap.put(entry.getKey(), values);
            }
            values.add(entry.getValue());
        });
        this.headers = Collections.unmodifiableMap(headersMap);
    }
}
