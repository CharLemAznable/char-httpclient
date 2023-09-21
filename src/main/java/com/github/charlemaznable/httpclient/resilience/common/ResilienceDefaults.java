package com.github.charlemaznable.httpclient.resilience.common;

import java.util.function.Predicate;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

public interface ResilienceDefaults {

    Predicate<Object> DEFAULT_RECORD_RESULT_PREDICATE = (Object object) -> false;

    @SuppressWarnings("unchecked")
    static Predicate<Object> checkRecordResultPredicate(Predicate<?> recordResultPredicate) {
        return nullThen((Predicate<Object>) recordResultPredicate, () -> DEFAULT_RECORD_RESULT_PREDICATE);
    }
}
