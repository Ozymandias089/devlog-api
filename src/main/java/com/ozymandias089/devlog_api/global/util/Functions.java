package com.ozymandias089.devlog_api.global.util;

import com.ozymandias089.devlog_api.global.enums.Role;

public class Functions {
    public static Role fromString(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    public static String roleToString(Role role) {
        return role.toString();
    }
}
