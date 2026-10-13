package com.chunkvault.exception;

public class ChunkMissingException extends RuntimeException {
    public ChunkMissingException(String message) {
        super(message);
    }
}
