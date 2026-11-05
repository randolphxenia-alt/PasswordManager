package com.passwordmanager.dao;

import com.passwordmanager.db.JdbcExecutor;
import com.passwordmanager.model.UserCredentials;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Доступ к таблице {@code users}.
 */
public class UserDAO {

    /** Запрос проверки существования пользователя. */
    private static final String SQL_EXISTS = "SELECT 1 FROM users WHERE username = ?";

    /** Запрос добавления пользователя. */
    private static final String SQL_INSERT = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

    /** Запрос поиска учётных данных по имени. */
    private static final String SQL_FIND = "SELECT id, username, password_hash FROM users WHERE username = ?";

    /** Создаёт DAO; соединение с БД открывается отдельно для каждой операции. */
    public UserDAO() {
    }

    /**
     * Проверяет, занято ли имя пользователя.
     *
     * @param username имя пользователя
     * @return {@code true}, если пользователь существует
     * @throws SQLException при ошибке БД
     */
    public boolean exists(String username) throws SQLException {
        return JdbcExecutor.queryFirst(SQL_EXISTS, resultSet -> Boolean.TRUE, username).isPresent();
    }

    /**
     * Добавляет нового пользователя.
     *
     * @param username     имя пользователя
     * @param passwordHash PBKDF2-хеш мастер-пароля
     * @throws SQLException при ошибке БД (в т.ч. нарушении уникальности имени)
     */
    public void insert(String username, String passwordHash) throws SQLException {
        JdbcExecutor.update(SQL_INSERT, username, passwordHash);
    }

    /**
     * Ищет учётные данные пользователя по имени.
     *
     * @param username имя пользователя
     * @return учётные данные или пустой {@link Optional}
     * @throws SQLException при ошибке БД
     */
    public Optional<UserCredentials> findByUsername(String username) throws SQLException {
        return JdbcExecutor.queryFirst(SQL_FIND, UserDAO::mapCredentials, username);
    }

    /**
     * Преобразует строку результата в {@link UserCredentials}.
     *
     * @param resultSet результат запроса
     * @return учётные данные пользователя
     * @throws SQLException при ошибке чтения
     */
    private static UserCredentials mapCredentials(ResultSet resultSet) throws SQLException {
        return new UserCredentials(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"));
    }
}