package com.passwordmanager.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
public final class MD5Service {
    private static final String ALGORITHM = "MD5";

    private MD5Service() {
    }
    public static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Алгоритм MD5 недоступен", e);
        }
    }
}