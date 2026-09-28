package com.example.medialibrary.backend.utils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class DyanmicSort {
    public static class SortKey<T, U extends Comparable<U>> {
        private final Function<T, U> keyExtractor;
        private final boolean ascending;

        public SortKey(Function<T, U> keyExtractor, boolean ascending) {
            this.keyExtractor = keyExtractor;
            this.ascending = ascending;
        }

        public Comparator<T> toComparator() {
            Comparator<T> cmp = Comparator.comparing(keyExtractor, Comparator.nullsLast(Comparator.naturalOrder()));
            return ascending ? cmp : cmp.reversed();
        }
    }

    public static <T> Comparator<T> buildDynamicComparator(List<SortKey<T, ?>> sortKeys) {
        if (sortKeys == null || sortKeys.isEmpty()) {
            return (a, b) -> 0; // No-op comparator
        }

        Comparator<T> comparator = (Comparator<T>) sortKeys.get(0).toComparator();
        for (int i = 1; i < sortKeys.size(); i++) {
            comparator = comparator.thenComparing((Comparator<T>) sortKeys.get(i).toComparator());
        }
        return comparator;
    }
}
