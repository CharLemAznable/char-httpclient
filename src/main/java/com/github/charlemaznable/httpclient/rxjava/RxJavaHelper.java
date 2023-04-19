package com.github.charlemaznable.httpclient.rxjava;

import com.github.charlemaznable.core.lang.ClzPath;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class RxJavaHelper {

    private static final String RXJAVA_SINGLE = "rx.Single";
    private static final String RXJAVA2_SINGLE = "io.reactivex.Single";
    private static final String RXJAVA3_SINGLE = "io.reactivex.rxjava3.core.Single";

    public static final boolean HAS_RXJAVA = ClzPath.classExists(RXJAVA_SINGLE);
    public static final boolean HAS_RXJAVA2 = ClzPath.classExists(RXJAVA2_SINGLE);
    public static final boolean HAS_RXJAVA3 = ClzPath.classExists(RXJAVA3_SINGLE);

    public static boolean checkReturnRxJavaSingle(Class<?> returnType) {
        return HAS_RXJAVA && Objects.equals(ClzPath.findClass(RXJAVA_SINGLE), returnType);
    }

    public static boolean checkReturnRxJava2Single(Class<?> returnType) {
        return HAS_RXJAVA2 && Objects.equals(ClzPath.findClass(RXJAVA2_SINGLE), returnType);
    }

    public static boolean checkReturnRxJava3Single(Class<?> returnType) {
        return HAS_RXJAVA3 && Objects.equals(ClzPath.findClass(RXJAVA3_SINGLE), returnType);
    }
}
