package com.passwordmanager.dao;

import com.passwordmanager.db.JdbcExecutor;
import com.passwordmanager.model.PasswordEntry;
import com.passwordmanager.model.StorageMode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * CRUD-операции с таблицей {@code password_entries}.
 * Все операции ограничены записями указанного пользователя.
 */
public class PasswordEntryDAO {

    /** Столбцы, выбираемые из таблицы записей. */
    private static final String COLUMNS =
            "SELECT id, user_id, title, website, login, password_data, storage_mode FROM password_entries ";

    /** Запрос добавления записи. */
    private static final String SQL_INSERT = """
            INSERT INTO password_entries
                (user_id, title, website, login, password_data, storage_mode)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    /** Запрос выборки всех записей пользователя. */
    private static final String SQL_FIND_ALL = COLUMNS + "WHERE user_id = ?";

    /** Запрос выборки одной записи пользователя. */
    private static final String SQL_FIND_ONE = COLUMNS + "WHERE id = ? AND user_id = ?";

    /** Запрос изменения записи. */
    private static final String SQL_UPDATE = """
            UPDATE password_entries
            SET title = ?, website = ?, login = ?, password_data = ?, storage_mode = ?
            WHERE id = ? AND user_id = ?
            """;

    /** Запрос удаления записи. */
    private static final String SQL_DELETE = "DELETE FROM password_entries WHERE id = ? AND user_id = ?";

    /** Создаёт DAO; соединение с БД открывается отдельно для каждой операции. */
    public PasswordEntryDAO() {
    }

    /**
     * Добавляет запись. Пароль должен быть уже защищён.
     *
     * @param entry запись (поле {@code id} игнорируется)
     * @throws SQLException при ошибке БД
     */
    public void add(PasswordEntry entry) throws SQLException {
        JdbcExecutor.update(SQL_INSERT, entry.userId(), entry.title(), entry.website(),
                entry.login(), entry.passwordData(), entry.storageMode().name());
    }

    /**
     * Возвращает все записи пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список записей (может быть пустым)
     * @throws SQLException при ошибке БД
     */
    public List<PasswordEntry> findAllByUser(int userId) throws SQLException {
        return JdbcExecutor.query(SQL_FIND_ALL, PasswordEntryDAO::mapEntry, userId);
    }

    /**
     * Ищет запись пользователя по идентификатору.
     *
     * @param userId  идентификатор пользователя
     * @param entryId идентификатор записи
     * @return запись или пустой {@link Optional}
     * @throws SQLException при ошибке БД
     */
    public Optional<PasswordEntry> findById(int userId, int entryId) throws SQLException {
        return JdbcExecutor.queryFirst(SQL_FIND_ONE, PasswordEntryDAO::mapEntry, entryId, userId);
    }

    /**
     * Изменяет запись пользователя.
     *
     * @param entry запись с новыми данными (поиск по {@code id} и {@code userId})
     * @return {@code true}, если запись найдена и изменена
     * @throws SQLException при ошибке БД
     */
    public boolean update(PasswordEntry entry) throws SQLException {
        return JdbcExecutor.update(SQL_UPDATE, entry.title(), entry.website(), entry.login(),
                entry.passwordData(), entry.storageMode().name(), entry.id(), entry.userId()) > 0;
    }

    /**
     * Удаляет запись пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param entryId идентификатор записи
     * @return {@code true}, если запись найдена и удалена
     * @throws SQLException при ошибке БД
     */
    public boolean delete(int userId, int entryId) throws SQLException {
        return JdbcExecutor.update(SQL_DELETE, entryId, userId) > 0;
    }

    /**
     * Преобразует строку результата в {@link PasswordEntry}.
     *
     * @param resultSet результат запроса
     * @return запись
     * @throws SQLException при ошибке чтения или неизвестном способе защиты
     */
    private static PasswordEntry mapEntry(ResultSet resultSet) throws SQLException {
        String modeName = resultSet.getString("storage_mode");
        StorageMode mode = StorageMode.fromName(modeName)
                .orElseThrow(() -> new SQLException("Неизвестный способ защиты в БД: " + modeName));
        return new PasswordEntry(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getString("title"),
                resultSet.getString("website"),
                resultSet.getString("login"),
                resultSet.getString("password_data"),
                mode);
    }
}