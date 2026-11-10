package com.passwordmanager.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
public final class KeyVault {
    private static final String SEALED_FEISTEL_KEY =
            "/LdrayUXLroAWV26IY9IiPHUKzLo8iZgQA9pX8mwHHd93b7K5x/JClswp7pdqA9uEQ==";
    private static final String SEALED_SALT_PEPPER =
            "RjTFheUw+Y5OmxmWbz0MDSvGryhIZlMk+2gWHZhowtZDHFS6pp0rQYuAgtLvxIo=";
    private static final byte[] VAULT_SEED = {
            (byte) 0x3A, (byte) 0x91, (byte) 0x5C, (byte) 0xE7, (byte) 0x12, (byte) 0xB4,
            (byte) 0x6F, (byte) 0xD8, (byte) 0x29, (byte) 0x47, (byte) 0xAC, (byte) 0x03,
            (byte) 0xF1, (byte) 0x8E, (byte) 0x55, (byte) 0xC6};
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final int AES_KEY_LENGTH = 16;
    private KeyVault() {
    }
    public static String feistelKey() {
        return unseal(SEALED_FEISTEL_KEY);
    }
    public static String saltPepper() {
        return unseal(SEALED_SALT_PEPPER);
    }
    private static String unseal(String sealed) {
        try {
            byte[] payload = Base64.getDecoder().decode(sealed);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, vaultKey(), new GCMParameterSpec(TAG_LENGTH, payload, 0, IV_LENGTH));
            byte[] plain = cipher.doFinal(payload, IV_LENGTH, payload.length - IV_LENGTH);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new CryptoException("Хранилище ключей повреждено", e);
        }
    }
    private static SecretKeySpec vaultKey() throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(VAULT_SEED);
        return new SecretKeySpec(Arrays.copyOf(digest, AES_KEY_LENGTH), "AES");
    }
}