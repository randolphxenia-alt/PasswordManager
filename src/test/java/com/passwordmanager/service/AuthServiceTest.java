package com.passwordmanager.service;

import com.passwordmanager.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class AuthServiceTest extends DatabaseTestBase {


    private final AuthService authService = new AuthService();






    @Test
    @DisplayName("Регистрация + вход")
    void registerAndLogin() throws AppException {
        authService.register("nikita", "secret1", "secret1");
        User user = authService.login("nikita", "secret1");
        assertEquals("nikita", user.username());
    }






    @Test
    @DisplayName("Пробелы вокруг имени игнорируются")
    void usernameIsTrimmed() throws AppException {
        authService.register("  nikita  ", "secret1", "secret1");
        assertEquals("nikita", authService.login("nikita ", "secret1").username());
    }






    @Test
    @DisplayName("Повторная регистрация → ошибка")
    void duplicateRegistration() throws AppException {
        authService.register("nikita", "secret1", "secret1");
        AppException error = assertThrows(AppException.class,
                () -> authService.register("nikita", "other12", "other12"));
        assertEquals("Пользователь «nikita» уже существует", error.getMessage());
    }






    @Test
    @DisplayName("Неверный пароль / неизвестный пользователь → одинаковая ошибка")
    void invalidCredentials() throws AppException {
        authService.register("nikita", "secret1", "secret1");
        AppException wrongPassword = assertThrows(AppException.class, () -> authService.login("nikita", "wrong"));
        AppException unknownUser = assertThrows(AppException.class, () -> authService.login("ghost", "secret1"));
        assertEquals(AuthService.INVALID_CREDENTIALS, wrongPassword.getMessage());
        assertEquals(AuthService.INVALID_CREDENTIALS, unknownUser.getMessage());
    }


    @Test
    @DisplayName("Пустые поля при входе → ошибка")
    void emptyLogin() {
        assertThrows(AppException.class, () -> authService.login("  ", ""));
        assertThrows(AppException.class, () -> authService.login(null, null));
    }
}