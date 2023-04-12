package com.github.charlemaznable.httpclient.vxclient.internal;

import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class VxDummy {

    @Nonnull
    private Class<?> implClass;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VxDummy && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "VxClient:" + implClass.getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
