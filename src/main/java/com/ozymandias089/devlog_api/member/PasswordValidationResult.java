package com.ozymandias089.devlog_api.member;

import java.util.List;

public record PasswordValidationResult(boolean validity, List<String> errors) {}
