package com.github.charlemaznable.httpclient.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import javax.annotation.Nullable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.lang.Condition.checkNotEmpty;
import static com.github.charlemaznable.core.lang.Rand.randInt;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingBalance {

    Class<? extends MappingBalancer> value();

    interface MappingBalancer {

        String choose(List<String> urls);
    }

    class RandomBalancer implements MappingBalancer {

        @Override
        public String choose(List<String> urls) {
            checkNotEmpty(urls);
            if (1 == urls.size()) return urls.get(0);
            return urls.get(randInt(urls.size()));
        }
    }

    class RoundRobinBalancer implements MappingBalancer {

        private final AtomicInteger cyclicCounter = new AtomicInteger(0);

        @Override
        public String choose(List<String> urls) {
            checkNotEmpty(urls);
            if (1 == urls.size()) return urls.get(0);
            return urls.get(getAndIncrementMod(urls.size()));
        }

        private int getAndIncrementMod(int size) {
            while (true) {
                int curr = cyclicCounter.get();
                int next = (curr + 1) % size;
                if (cyclicCounter.compareAndSet(curr, next)) return next;
            }
        }
    }

    @AllArgsConstructor
    enum BalanceType {

        RANDOM(new RandomBalancer()),
        ROUND_ROBIN(new RoundRobinBalancer());

        @Getter
        private final MappingBalancer mappingBalancer;

        @Nullable
        public static BalanceType resolve(String balanceTypeName) {
            for (val balanceType : values()) {
                if (balanceType.name().equalsIgnoreCase(balanceTypeName)) {
                    return balanceType;
                }
            }
            return null;
        }
    }
}
