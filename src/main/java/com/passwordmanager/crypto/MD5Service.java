package com.passwordmanager.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * MD5-хеширование строк.
 * <p>
 * MD5 считается криптографически устаревшим и оставлен в приложении
 * как один из демонстрационных режимов хранения.
 */
public final class MD5Service {

    /** Имя алгоритма в Java Cryptography Architecture. */
    private static final String ALGORITHM = "MD5";

    /** Закрытый конструктор: класс содержит только статические методы. */
    private MD5Service() {
    }

    /**
     * Вычисляет MD5-хеш строки.
     *
     * @param text исходный текст
     * @return хеш в виде 32 шестнадцатеричных символов в нижнем регистре
     * @throws CryptoException если алгоритм MD5 недоступен в JVM
     */
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