package com.passwordmanager.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FeistelCipherTest {


    private static final String KEY = "secret-key";






    @ParameterizedTest
    @ValueSource(strings = {"", "a", "ab", "Password123!", "Привет, мир!", "😀 emoji"})
    @DisplayName("encrypt → decrypt возвращает исходный текст")
    void roundTrip(String text) {
        Assertions.assertEquals(text, FeistelCipher.decrypt(FeistelCipher.encrypt(text, KEY), KEY));
    }


    @Test
    @DisplayName("Шифртекст не совпадает с открытым текстом")
    void cipherTextDiffersFromPlainText() {
        assertNotEquals("Password123!", FeistelCipher.encrypt("Password123!", KEY));
    }


    @Test
    @DisplayName("Другой ключ не расшифровывает данные")
    void wrongKeyGivesDifferentText() {
        String cipher = FeistelCipher.encrypt("Password123!", KEY);
        assertNotEquals("Password123!", FeistelCipher.decrypt(cipher, "other-key"));
    }


    @Test
    @DisplayName("Пустой ключ → CryptoException")
    void emptyKeyRejected() {
        assertThrows(CryptoException.class, () -> FeistelCipher.encrypt("text", ""));
        assertThrows(CryptoException.class, () -> FeistelCipher.encrypt("text", null));
    }


    @Test
    @DisplayName("Повреждённые данные → CryptoException")
    void corruptedDataRejected() {
        assertThrows(CryptoException.class, () -> FeistelCipher.decrypt("не base64!", KEY));
        assertThrows(CryptoException.class, () -> FeistelCipher.decrypt("AAE=", KEY));
        assertThrows(CryptoException.class, () -> FeistelCipher.decrypt("AAAAZAAA", KEY));
    }
}