package com.passwordmanager.crypto;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Пара «соль + хеш» и её строковое представление {@code соль:хеш} (обе части в Base64).
 * Общий формат для всех хешей с солью в приложении.
 *
 * @param salt соль
 * @param hash хеш
 */
record SaltedValue(byte[] salt, byte[] hash) {

    /** Длина соли в байтах. */
    static final int SALT_LENGTH = 16;

    /** Разделитель соли и хеша в строке. */
    private static final String SEPARATOR = ":";

    /** Криптографически стойкий генератор случайных чисел. */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Генерирует случайную соль.
     *
     * @return массив случайных байт длины {@link #SALT_LENGTH}
     */
    static byte[] randomSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Разбирает строку {@code соль:хеш}.
     *
     * @param stored сохранённая строка
     * @return пара «соль + хеш»
     * @throws CryptoException если формат строки неверный
     */
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

    /**
     * Формирует строку для хранения.
     *
     * @return строка {@code соль:хеш}
     */
    String format() {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(salt) + SEPARATOR + encoder.encodeToString(hash);
    }
}