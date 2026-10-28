package org.demo.backend.global.jwt.domain;

public class BlacklistedTokenException extends RuntimeException {

    public BlacklistedTokenException(String message) {
        super(message);
    }
}
