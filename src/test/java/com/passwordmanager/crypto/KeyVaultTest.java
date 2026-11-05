package com.passwordmanager.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/** Тесты защищённого хранилища ключей. */
class KeyVaultTest {

    /** Ключи расшифровываются, не пустые и различаются. */
    @Test
    @DisplayName("Ключи расшифровываются и различаются")
    void keysAreRestored() {
        Assertions.assertFalse(KeyVault.feistelKey().isBlank());
        assertFalse(KeyVault.saltPepper().isBlank());
        assertNotEquals(KeyVault.feistelKey(), KeyVault.saltPepper());
    }

    /** Ключ при каждом обращении одинаковый — иначе ранее зашифрованные данные не расшифровать. */
    @Test
    @DisplayName("Ключ Фейстеля стабилен между обращениями")
    void keysAreStable() {
        assertEquals(KeyVault.feistelKey(), KeyVault.feistelKey());
    }
}