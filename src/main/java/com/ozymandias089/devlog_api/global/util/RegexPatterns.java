package com.ozymandias089.devlog_api.global.util;

import java.util.regex.Pattern;

public class RegexPatterns {
    /**
     * Regular expression pattern for validating email addresses.
     * <p>
     * Rules:
     * <ul>
     *     <li>Must contain only alphanumeric characters, plus sign (+), underscore (_), period (.) or hyphen (-) before the '@'.</li>
     *     <li>Must contain a valid domain name after the '@', allowing alphanumeric characters, periods, and hyphens.</li>
     *     <li>Must end with a top-level domain (TLD) of at least two letters (e.g., .com, .net, .org).</li>
     * </ul>
     * <p>Example valid emails: {@code user@example.com}, {@code first.last@mail-server.org}, {@code name+tag@gmail.com}</p>
     */
    public static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Regular expression pattern for validating usernames.
     * <p>
     * Rules:
     * <ul>
     *     <li>Allowed characters: uppercase A-Z, lowercase a-z, digits 0-9, underscore (_), and hyphen (-).</li>
     *     <li>Length: minimum 3 characters, maximum 20 characters.</li>
     *     <li>No spaces or special characters other than underscore and hyphen are allowed.</li>
     * </ul>
     * <p>Example valid usernames: {@code user123}, {@code my_name}, {@code user-name}</p>
     */
    public static final Pattern USERNAME_REGEX = Pattern.compile("^[A-Za-z0-9_-]{3,20}$");

    private RegexPatterns() {
        throw new IllegalStateException("Utility class");
    }
}
