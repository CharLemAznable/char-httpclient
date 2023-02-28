package com.github.charlemaznable.httpclient.ohclient.internal;

import lombok.AllArgsConstructor;
import okhttp3.ConnectionPool;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Properties;

import static com.github.charlemaznable.core.config.Arguments.argumentsAsProperties;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsProperties;
import static com.github.charlemaznable.core.lang.Propertiess.ssMap;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class OhDummy {

    static final Logger log = LoggerFactory.getLogger("OhClient");
    static final ConnectionPool ohConnectionPool;
    static Properties ohClassPathProperties;

    static {
        ohConnectionPool = new ConnectionPool();
    }

    static String substitute(String source) {
        if (isNull(ohClassPathProperties)) {
            ohClassPathProperties = classResourceAsProperties("ohclient.env.props");
        }
        return new StringSubstitutor(ssMap(argumentsAsProperties(
                ohClassPathProperties))).replace(source);
    }

    @Nonnull
    private Class<?> implClass;

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
        return "OhClient:" + implClass.getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
