package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.httpclient.ohclient.elf.OhConnectionPoolElf;
import com.github.charlemaznable.httpclient.ohclient.elf.OhDispatcherElf;
import lombok.AllArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
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

    public static final Logger log = LoggerFactory.getLogger("OhClient");
    public static final Dispatcher ohDispatcher;
    public static final ConnectionPool ohConnectionPool;
    static Properties ohClassPathProperties;

    static {
        ohDispatcher = OhDispatcherElf.newDispatcher();
        ohConnectionPool = OhConnectionPoolElf.newConnectionPool();
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
