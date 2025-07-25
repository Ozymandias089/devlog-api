package com.ozymandias089.devlog_api.global.exception;

public class DuplicateEmailExcpetion extends RuntimeException {
    public DuplicateEmailExcpetion(String email) {
        super("Email " + email + "Already Exists");
    }
}
