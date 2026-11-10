package com.passwordmanager.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH = 256;
    private PasswordHasher() {
    }
    public static String hash(String password) {
        byte[] salt = SaltedValue.randomSalt();
        return new SaltedValue(salt, derive(password, salt)).format();
    }
    public static boolean verify(String password, String stored) {
        SaltedValue expected = SaltedValue.parse(stored);
        return MessageDigest.isEqual(derive(password, expected.salt()), expected.hash());
    }
    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Не удалось вычислить PBKDF2-хеш", e);
        } finally {
            spec.clearPassword();
        }
    }
}