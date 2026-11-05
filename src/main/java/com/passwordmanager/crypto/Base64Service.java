package com.passwordmanager.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Кодирование и декодирование строк в Base64.
 * <p>
 * Base64 — обратимое кодирование, а не шифрование: оно лишь скрывает текст
 * от случайного взгляда.
 */
public final class Base64Service {

    /** Закрытый конструктор: класс содержит только статические методы. */
    private Base64Service() {
    }

    /**
     * Кодирует строку в Base64 (UTF-8).
     *
     * @param text исходный текст
     * @return строка в Base64
     */
    public static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Декодирует строку из Base64 (UTF-8).
     *
     * @param encoded строка в Base64
     * @return исходный текст
     * @throws CryptoException если строка не является корректным Base64
     */
    public static String decode(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Некорректная строка Base64", e);
        }
    }
}