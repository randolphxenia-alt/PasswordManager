package com.passwordmanager.service;

import com.passwordmanager.model.StorageMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class StorageServiceTest {






    @ParameterizedTest
    @EnumSource(StorageMode.class)
    @DisplayName("matches работает во всех способах защиты")
    void matchesInEveryMode(StorageMode mode) {
        String stored = StorageService.encode("Секрет-123", mode);
        assertTrue(StorageService.matches("Секрет-123", stored, mode));
        assertFalse(StorageService.matches("другое", stored, mode));
    }






    @ParameterizedTest
    @EnumSource(StorageMode.class)
    @DisplayName("Пароль не хранится открытым текстом")
    void storedValueDiffersFromPassword(StorageMode mode) {
        assertNotEquals("Секрет-123", StorageService.encode("Секрет-123", mode));
    }






    @ParameterizedTest
    @EnumSource(value = StorageMode.class, names = {"BASE64", "FEISTEL"})
    @DisplayName("Обратимые способы: encode → decode")
    void reversibleRoundTrip(StorageMode mode) {
        String stored = StorageService.encode("P@ssw0rd", mode);
        assertEquals("P@ssw0rd", StorageService.decode(stored, mode).orElseThrow());
    }






    @ParameterizedTest
    @EnumSource(value = StorageMode.class, names = {"MD5", "SALTED_HASH"})
    @DisplayName("Хеш-способы: decode возвращает пустой результат")
    void hashModesAreIrreversible(StorageMode mode) {
        assertTrue(StorageService.decode(StorageService.encode("x", mode), mode).isEmpty());
    }


    @Test
    @DisplayName("StorageMode.fromName: поиск без учёта регистра")
    void fromName() {
        assertEquals(StorageMode.FEISTEL, StorageMode.fromName("feistel").orElseThrow());
        assertTrue(StorageMode.fromName("PLAINTEXT").isEmpty());
        assertTrue(StorageMode.fromName(null).isEmpty());
    }
}