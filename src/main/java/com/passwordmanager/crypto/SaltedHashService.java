package com.passwordmanager.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public final class SaltedHashService {
    private static final String ALGORITHM = "SHA-256";
    private SaltedHashService() {
    }
    public static String hash(String text, String pepper) {
        byte[] salt = SaltedValue.randomSalt();
        return new SaltedValue(salt, digest(salt, text, pepper)).format();
    }
    public static boolean verify(String text, String stored, String pepper) {
        SaltedValue expected = SaltedValue.parse(stored);
        return MessageDigest.isEqual(digest(expected.salt(), text, pepper), expected.hash());
    }
    private static byte[] digest(byte[] salt, String text, String pepper) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            digest.update(text.getBytes(StandardCharsets.UTF_8));
            digest.update(pepper.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Алгоритм SHA-256 недоступен", e);
        }
    }
}