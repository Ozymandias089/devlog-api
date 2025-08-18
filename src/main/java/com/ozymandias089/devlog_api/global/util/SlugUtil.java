package com.ozymandias089.devlog_api.global.util;

import lombok.RequiredArgsConstructor;

import java.text.Normalizer;
import java.util.Locale;

@RequiredArgsConstructor
public final class SlugUtil {
    public static String toSlug(String input) {
        if (input == null) return "post";

        // 분해 정규화 + 결합문자 제거(라틴 발음부호 등)
        String n = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 허용: 한글, 영문/숫자, 공백/하이픈
        String cleaned = n.replaceAll("[^\\p{IsAlphabetic}\\d\\uAC00-\\uD7A3\\s-]", "");
        String hyphened = cleaned.trim().replaceAll("\\s+", "-").replaceAll("-{2,}", "-");
        String lower = hyphened.toLowerCase(Locale.ROOT);
        if(lower.isBlank()) return "post";

        // 길이 제한(예: 100)
        return lower.length() > 100 ? lower.substring(0, 100) : lower;
    }

    public static String shortToken() {
        long v = Math.abs(Double.doubleToLongBits(Math.random()));
        String s = Long.toString(v, 36);
        return s.substring(Math.max(0, s.length()-6));
    }
}
