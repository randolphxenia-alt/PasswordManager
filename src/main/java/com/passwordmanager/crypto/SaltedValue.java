package com.passwordmanager.crypto;

import java.security.SecureRandom;
import java.util.Base64;
record SaltedValue(byte[] salt, byte[] hash) {
    static final int SALT_LENGTH = 16;
    private static final String SEPARATOR = ":";
    private static final SecureRandom RANDOM = new SecureRandom();
    static byte[] randomSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }
    static SaltedValue parse(String stored) {
        String[] parts = stored == null ? new String[0] : stored.split(SEPARATOR, 2);
        if (parts.length != 2) {
            throw new CryptoException("Повреждённый хеш: нет разделителя соли");
        }
        try {
            return new SaltedValue(Base64.getDecoder().decode(parts[0]), Base64.getDecoder().decode(parts[1]));
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Повреждённый хеш: некорректный Base64", e);
        }
    }
    String format() {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(salt) + SEPARATOR + encoder.encodeToString(hash);
    }
}