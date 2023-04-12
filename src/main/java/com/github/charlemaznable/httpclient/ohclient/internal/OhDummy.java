package com.github.charlemaznable.httpclient.ohclient.internal;

import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class OhDummy {

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
