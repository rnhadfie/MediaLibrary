package utils;

import java.util.regex.Pattern;

public class UUIDValidator {
    // Regex for standard 8-4-4-4-12 UUID format
    private static final Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static boolean isValidUUID(String candidate) {
        if (candidate == null) {
            return false;
        }
        return UUID_REGEX.matcher(candidate).matches();
    }
}
