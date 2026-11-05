package com.passwordmanager.service;

import com.passwordmanager.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Интеграционные тесты регистрации и входа (с временной БД SQLite). */
class AuthServiceTest extends DatabaseTestBase {

    /** Тестируемый сервис. */
    private final AuthService authService = new AuthService();

    /**
     * После регистрации пользователь может войти.
     *
     * @throws AppException при ошибке сервиса
     */
    @Test
    @DisplayName("Регистрация + вход")
    void registerAndLogin() throws AppException {
        authService.register("nikita", "secret1", "secret1");
        User user = authService.login("nikita", "secret1");
        assertEquals("nikita", user.username());
    }

    /**
     * Пробелы вокруг имени не мешают входу.
     *
     * @throws AppException при ошибке сервиса
     */
    @Test
    @DisplayName("Пробелы вокруг имени игнорируются")
    void usernameIsTrimmed() throws AppException {
        authService.register("  nikita  ", "secret1", "secret1");
        assertEquals("nikita", authService.login("nikita ", "secret1").username());
    }

    /**
     * Повторная регистрация того же имени отклоняется.
     *
     * @throws AppException при ошибке первой регистрации
     */
    @Test
    @DisplayName("Повторная регистрация → ошибка")
    void duplicateRegistration() throws AppException {
        authService.register("nikita", "secret1", "secret1");
        AppException error = assertThrows(AppException.class,
                () -> authService.register("nikita", "other12", "other12"));
        assertEquals("Пользователь «nikita» уже существует", error.getMessage());
    }

    /**
     * Неверный пароль и неизвестный пользователь дают одинаковое сообщение.
     *
     * @throws AppException при ошибке регистрации
     */
    @Test
    @DisplayName("Неверный пароль / неизвестный пользователь → одинаковая ошибка")
    void invalidCredentials() throws AppException {
        authService.register("nikita", "secret1", "secret1");
        AppException wrongPassword = assertThrows(AppException.class, () -> authService.login("nikita", "wrong"));
        AppException unknownUser = assertThrows(AppException.class, () -> authService.login("ghost", "secret1"));
        assertEquals(AuthService.INVALID_CREDENTIALS, wrongPassword.getMessage());
        assertEquals(AuthService.INVALID_CREDENTIALS, unknownUser.getMessage());
    }

    /** Пустые поля при входе отклоняются без обращения к БД. */
    @Test
    @DisplayName("Пустые поля при входе → ошибка")
    void emptyLogin() {
        assertThrows(AppException.class, () -> authService.login("  ", ""));
        assertThrows(AppException.class, () -> authService.login(null, null));
    }
}