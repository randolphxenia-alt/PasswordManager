package com.passwordmanager.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Base64Service {
    private Base64Service() {
    }
    public static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
    public static String decode(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Некорректная строка Base64", e);
        }
    }
}