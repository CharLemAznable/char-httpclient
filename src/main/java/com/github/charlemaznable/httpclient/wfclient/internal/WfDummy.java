package com.github.charlemaznable.httpclient.wfclient.internal;

import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class WfDummy {

    @Nonnull
    private Class<?> implClass;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WfDummy && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "WfClient:" + implClass.getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
