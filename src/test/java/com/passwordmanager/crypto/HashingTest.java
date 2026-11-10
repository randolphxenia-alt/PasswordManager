package com.passwordmanager.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HashingTest {


    private static final String PEPPER = "test-pepper";


    @Test
    @DisplayName("Base64: encode → decode")
    void base64RoundTrip() {
        String text = "Привет, мир!";
        Assertions.assertEquals(text, Base64Service.decode(Base64Service.encode(text)));
    }


    @Test
    @DisplayName("Base64: некорректная строка → CryptoException")
    void base64InvalidInput() {
        assertThrows(CryptoException.class, () -> Base64Service.decode("###"));
    }


    @Test
    @DisplayName("MD5(\"abc\") совпадает с эталоном RFC 1321")
    void md5KnownValue() {
        Assertions.assertEquals("900150983cd24fb0d6963f7d28e17f72", MD5Service.hash("abc"));
    }


    @Test
    @DisplayName("Хеш с солью: верный пароль принимается, неверный — нет")
    void saltedHashVerify() {
        String stored = SaltedHashService.hash("qwerty", PEPPER);
        assertTrue(SaltedHashService.verify("qwerty", stored, PEPPER));
        assertFalse(SaltedHashService.verify("qwerty1", stored, PEPPER));
    }


    @Test
    @DisplayName("Хеш с солью: одинаковые пароли → разные хеши")
    void saltMakesHashesUnique() {
        assertNotEquals(SaltedHashService.hash("same", PEPPER), SaltedHashService.hash("same", PEPPER));
    }


    @Test
    @DisplayName("Хеш с солью: другой «перец» → пароль не подходит")
    void wrongPepperFails() {
        String stored = SaltedHashService.hash("qwerty", PEPPER);
        assertFalse(SaltedHashService.verify("qwerty", stored, "other-pepper"));
    }


    @Test
    @DisplayName("PBKDF2: верный пароль принимается, неверный — нет")
    void pbkdf2Verify() {
        String stored = PasswordHasher.hash("pass123");
        assertTrue(PasswordHasher.verify("pass123", stored));
        assertFalse(PasswordHasher.verify("wrong", stored));
    }


    @Test
    @DisplayName("Повреждённый хеш → CryptoException")
    void corruptedHash() {
        assertThrows(CryptoException.class, () -> PasswordHasher.verify("x", "без-разделителя"));
        assertThrows(CryptoException.class, () -> SaltedHashService.verify("x", "###:###", PEPPER));
    }
}