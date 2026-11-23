package com.chunkvault.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ChecksumUtil {

    private ChecksumUtil() {}

    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String expectedHex, byte[] data) {
        if (expectedHex == null || expectedHex.isBlank()) {
            return true; // legacy rows without checksums are not verified
        }
        return sha256Hex(data).equalsIgnoreCase(expectedHex);
    }
}
