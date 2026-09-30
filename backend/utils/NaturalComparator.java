package utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalComparator<T> implements Comparator<T> {

    private final Function<T, String> keyExtractor;
    private static final Pattern PATTERN = Pattern.compile("(\\d+|\\D+)");

    // Constructor accepts a function to extract the string property from the object
    public NaturalComparator(Function<T, String> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    @Override
    public int compare(T o1, T o2) {
        // Handle null objects safely
        if (o1 == o2) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;

        String s1 = keyExtractor.apply(o1);
        String s2 = keyExtractor.apply(o2);

        // Handle null property values safely
        if (Objects.equals(s1, s2)) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;

        Matcher m1 = PATTERN.matcher(s1);
        Matcher m2 = PATTERN.matcher(s2);

        while (m1.find() && m2.find()) {
            String chunk1 = m1.group();
            String chunk2 = m2.group();

            if (Character.isDigit(chunk1.charAt(0)) && Character.isDigit(chunk2.charAt(0))) {
                int num1 = Integer.parseInt(chunk1);
                int num2 = Integer.parseInt(chunk2);
                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            } else {
                int textCompare = chunk1.compareTo(chunk2);
                if (textCompare != 0) {
                    return textCompare;
                }
            }
        }

        return Integer.compare(s1.length(), s2.length());
    }
}