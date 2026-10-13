package com.chunkvault.exception;

public class InvalidUploadException extends RuntimeException {
    public InvalidUploadException(String message) {
        super(message);
    }
}
