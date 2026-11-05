package com.passwordmanager.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты Base64, MD5, хеша с солью и PBKDF2. */
class HashingTest {

    /** «Перец», используемый в тестах хеша с солью. */
    private static final String PEPPER = "test-pepper";

    /** Base64 корректно кодирует и декодирует кириллицу. */
    @Test
    @DisplayName("Base64: encode → decode")
    void base64RoundTrip() {
        String text = "Привет, мир!";
        Assertions.assertEquals(text, Base64Service.decode(Base64Service.encode(text)));
    }

    /** Некорректный Base64 отклоняется. */
    @Test
    @DisplayName("Base64: некорректная строка → CryptoException")
    void base64InvalidInput() {
        assertThrows(CryptoException.class, () -> Base64Service.decode("###"));
    }

    /** MD5 совпадает с эталонным значением из RFC 1321. */
    @Test
    @DisplayName("MD5(\"abc\") совпадает с эталоном RFC 1321")
    void md5KnownValue() {
        Assertions.assertEquals("900150983cd24fb0d6963f7d28e17f72", MD5Service.hash("abc"));
    }

    /** Хеш с солью принимает верный пароль и отклоняет неверный. */
    @Test
    @DisplayName("Хеш с солью: верный пароль принимается, неверный — нет")
    void saltedHashVerify() {
        String stored = SaltedHashService.hash("qwerty", PEPPER);
        assertTrue(SaltedHashService.verify("qwerty", stored, PEPPER));
        assertFalse(SaltedHashService.verify("qwerty1", stored, PEPPER));
    }

    /** Одинаковые пароли дают разные хеши благодаря случайной соли. */
    @Test
    @DisplayName("Хеш с солью: одинаковые пароли → разные хеши")
    void saltMakesHashesUnique() {
        assertNotEquals(SaltedHashService.hash("same", PEPPER), SaltedHashService.hash("same", PEPPER));
    }

    /** Без правильного «перца» хеш не проверяется. */
    @Test
    @DisplayName("Хеш с солью: другой «перец» → пароль не подходит")
    void wrongPepperFails() {
        String stored = SaltedHashService.hash("qwerty", PEPPER);
        assertFalse(SaltedHashService.verify("qwerty", stored, "other-pepper"));
    }

    /** PBKDF2 принимает верный пароль и отклоняет неверный. */
    @Test
    @DisplayName("PBKDF2: верный пароль принимается, неверный — нет")
    void pbkdf2Verify() {
        String stored = PasswordHasher.hash("pass123");
        assertTrue(PasswordHasher.verify("pass123", stored));
        assertFalse(PasswordHasher.verify("wrong", stored));
    }

    /** Повреждённые хеши отклоняются. */
    @Test
    @DisplayName("Повреждённый хеш → CryptoException")
    void corruptedHash() {
        assertThrows(CryptoException.class, () -> PasswordHasher.verify("x", "без-разделителя"));
        assertThrows(CryptoException.class, () -> SaltedHashService.verify("x", "###:###", PEPPER));
    }
}