package com.ozymandias089.devlog_api.global.util;

import java.util.regex.Pattern;

public class RegexPatterns {
    public static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private RegexPatterns() {
        throw new IllegalStateException("Utility class");
    }
}
