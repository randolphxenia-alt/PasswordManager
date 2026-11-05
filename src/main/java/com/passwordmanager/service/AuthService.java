package com.passwordmanager.service;

import com.passwordmanager.crypto.CryptoException;
import com.passwordmanager.crypto.PasswordHasher;
import com.passwordmanager.dao.UserDAO;
import com.passwordmanager.model.User;
import com.passwordmanager.model.UserCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

/**
 * Регистрация и вход пользователей.
 * <p>
 * Мастер-пароль всегда хранится в одном защищённом формате — PBKDF2-HMAC-SHA256
 * со случайной солью (см. {@link PasswordHasher}); пользователь формат не выбирает.
 */
public class AuthService {

    /** Сообщение при неверной паре «логин — пароль» (одинаковое, чтобы не раскрывать, что именно неверно). */
    static final String INVALID_CREDENTIALS = "Неверное имя пользователя или пароль";

    /** Логгер класса. */
    private static final Logger log = LogManager.getLogger(AuthService.class);

    /** DAO пользователей. */
    private final UserDAO userDAO;

    /** Создаёт сервис с DAO по умолчанию. */
    public AuthService() {
        this(new UserDAO());
    }

    /**
     * Создаёт сервис с указанным DAO (используется в тестах).
     *
     * @param userDAO DAO пользователей
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param username     имя пользователя
     * @param password     мастер-пароль
     * @param confirmation повтор мастер-пароля
     * @throws AppException если данные некорректны, имя занято или БД недоступна
     */
    public void register(String username, String password, String confirmation) throws AppException {
        String name = InputValidator.normalize(username);
        InputValidator.validateRegistration(name, password, confirmation);
        try {
            ensureUsernameFree(name);
            userDAO.insert(name, PasswordHasher.hash(password));
            log.info("Зарегистрирован пользователь: {}", name);
        } catch (SQLException | CryptoException e) {
            log.error("Ошибка регистрации пользователя {}", name, e);
            throw new AppException("Не удалось создать аккаунт: ошибка хранилища", e);
        }
    }

    /**
     * Выполняет вход.
     *
     * @param username имя пользователя
     * @param password мастер-пароль
     * @return вошедший пользователь
     * @throws AppException если пара «логин — пароль» неверна или БД недоступна
     */
    public User login(String username, String password) throws AppException {
        String name = InputValidator.normalize(username);
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new AppException("Введите имя пользователя и пароль");
        }
        try {
            UserCredentials credentials = findCredentials(name);
            verifyPassword(credentials, password);
            log.info("Вход выполнен: {}", name);
            return credentials.toUser();
        } catch (SQLException | CryptoException e) {
            log.error("Ошибка входа пользователя {}", name, e);
            throw new AppException("Не удалось выполнить вход: ошибка хранилища", e);
        }
    }

    /**
     * Проверяет, что имя пользователя не занято.
     *
     * @param username имя пользователя
     * @throws AppException если имя занято
     * @throws SQLException при ошибке БД
     */
    private void ensureUsernameFree(String username) throws AppException, SQLException {
        if (userDAO.exists(username)) {
            log.warn("Попытка регистрации существующего пользователя: {}", username);
            throw new AppException("Пользователь «" + username + "» уже существует");
        }
    }

    /**
     * Загружает учётные данные пользователя.
     *
     * @param username имя пользователя
     * @return учётные данные
     * @throws AppException если пользователь не найден
     * @throws SQLException при ошибке БД
     */
    private UserCredentials findCredentials(String username) throws AppException, SQLException {
        return userDAO.findByUsername(username).orElseThrow(() -> {
            log.warn("Вход: пользователь не найден: {}", username);
            return new AppException(INVALID_CREDENTIALS);
        });
    }

    /**
     * Сверяет введённый пароль с сохранённым хешем.
     *
     * @param credentials учётные данные из БД
     * @param password    введённый пароль
     * @throws AppException если пароль неверный
     */
    private void verifyPassword(UserCredentials credentials, String password) throws AppException {
        if (!PasswordHasher.verify(password, credentials.passwordHash())) {
            log.warn("Вход: неверный пароль для {}", credentials.username());
            throw new AppException(INVALID_CREDENTIALS);
        }
    }
}