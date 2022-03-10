package com.github.charlemaznable.httpclient.ohclient.internal;

import lombok.NoArgsConstructor;
import okhttp3.ConnectionPool;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static com.github.charlemaznable.core.config.Arguments.argumentsAsProperties;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsProperties;
import static com.github.charlemaznable.core.lang.Propertiess.ssMap;
import static java.util.Objects.isNull;
import static java.util.concurrent.Executors.newCachedThreadPool;

@NoArgsConstructor
public class OhDummy {

    static final Logger log = LoggerFactory.getLogger("OhClient");
    static final ExecutorService ohExecutorService;
    static final ConnectionPool ohConnectionPool;
    static Properties ohClassPathProperties;

    static {
        ohExecutorService = newCachedThreadPool();
        ohConnectionPool = new ConnectionPool();
    }

    static String substitute(String source) {
        if (isNull(ohClassPathProperties)) {
            ohClassPathProperties = classResourceAsProperties("ohclient.env.props");
        }
        return new StringSubstitutor(ssMap(argumentsAsProperties(
                ohClassPathProperties))).replace(source);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OhDummy && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "OhClient@" + Integer.toHexString(hashCode());
    }
}
