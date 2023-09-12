package com.github.charlemaznable.httpclient.configurer.configservice;

import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ConfigurerElf {

    @SuppressWarnings("unchecked")
    @SneakyThrows
    static <T> List<Pair<String, T>> parseStringToPairList(String str) {
        val result = new ArrayList<Pair<String, T>>();
        Iterable<String> pairs = Splitter.on("&")
                .omitEmptyStrings().trimResults().split(str);
        for (val pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                result.add(Pair.of(decode(pair, UTF_8), null));
            } else {
                String name = decode(pair.substring(0, idx), UTF_8);
                String value = decode(pair.substring(idx + 1), UTF_8);
                result.add(Pair.of(name, (T) value));
            }
        }
        return result;
    }

    static <T> T parseStringToValue(String str, T defaultValue, Function<String, T> parser) {
        return checkBlank(str, () -> defaultValue, value -> {
            try {
                return parser.apply(value);
            } catch (Exception e) {
                return defaultValue;
            }
        });
    }
}
