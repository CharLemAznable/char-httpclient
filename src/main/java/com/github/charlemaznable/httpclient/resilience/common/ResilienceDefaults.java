package com.github.charlemaznable.httpclient.resilience.common;

import java.util.function.Predicate;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

public interface ResilienceDefaults {

    Predicate<Object> DEFAULT_RESULT_PREDICATE = (Object object) -> false;

    @SuppressWarnings("unchecked")
    static Predicate<Object> checkResultPredicate(Predicate<?> resultPredicate) {
        return nullThen((Predicate<Object>) resultPredicate, () -> DEFAULT_RESULT_PREDICATE);
    }
}
