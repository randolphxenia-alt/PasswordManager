package com.passwordmanager.service;

import com.passwordmanager.model.EntryData;
import com.passwordmanager.model.StorageMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Тесты проверки пользовательского ввода. */
class InputValidatorTest {

    /** Корректные данные регистрации проходят проверку. */
    @Test
    @DisplayName("Корректная регистрация проходит проверку")
    void validRegistration() {
        assertDoesNotThrow(() -> InputValidator.validateRegistration("nikita_01", "secret1", "secret1"));
    }

    /**
     * Некорректные имена пользователей отклоняются.
     *
     * @param username имя пользователя
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "ab", "имя", "user name", "a_very_long_username_more_than_32_chars"})
    @DisplayName("Некорректное имя пользователя отклоняется")
    void invalidUsername(String username) {
        assertThrows(AppException.class, () -> InputValidator.validateRegistration(username, "secret1", "secret1"));
    }

    /** Короткий пароль отклоняется. */
    @Test
    @DisplayName("Короткий мастер-пароль отклоняется")
    void shortPassword() {
        assertThrows(AppException.class, () -> InputValidator.validateRegistration("nikita", "123", "123"));
    }

    /** Несовпадающие пароли отклоняются. */
    @Test
    @DisplayName("Пароль и повтор не совпадают → ошибка")
    void passwordsMismatch() {
        AppException error = assertThrows(AppException.class,
                () -> InputValidator.validateRegistration("nikita", "secret1", "secret2"));
        assertEquals("Пароли не совпадают", error.getMessage());
    }

    /** Запись без обязательных полей отклоняется. */
    @Test
    @DisplayName("Запись без названия, логина или пароля отклоняется")
    void invalidEntry() {
        assertThrows(AppException.class, () -> InputValidator.validateEntry(
                new EntryData("", "", "login", "pass", StorageMode.BASE64), true));
        assertThrows(AppException.class, () -> InputValidator.validateEntry(
                new EntryData("GitHub", "", "", "pass", StorageMode.BASE64), true));
        assertThrows(AppException.class, () -> InputValidator.validateEntry(
                new EntryData("GitHub", "", "login", "", StorageMode.BASE64), true));
        assertThrows(AppException.class, () -> InputValidator.validateEntry(
                new EntryData("GitHub", "", "login", "pass", null), true));
    }

    /** При изменении записи пустой пароль допустим (означает «не менять»). */
    @Test
    @DisplayName("Пустой пароль допустим, если он необязателен")
    void optionalPassword() {
        assertDoesNotThrow(() -> InputValidator.validateEntry(
                new EntryData("GitHub", "", "login", "", StorageMode.MD5), false));
    }

    /** Нормализация убирает пробелы и обрабатывает null. */
    @Test
    @DisplayName("normalize убирает пробелы и заменяет null пустой строкой")
    void normalize() {
        assertEquals("abc", InputValidator.normalize("  abc  "));
        assertEquals("", InputValidator.normalize(null));
    }
}