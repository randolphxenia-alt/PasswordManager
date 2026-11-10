package com.passwordmanager.dao;

import com.passwordmanager.db.JdbcExecutor;
import com.passwordmanager.model.PasswordEntry;
import com.passwordmanager.model.StorageMode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PasswordEntryDAO {

    private static final String COLUMNS =
            "SELECT id, user_id, title, website, login, password_data, storage_mode FROM password_entries ";

    private static final String SQL_INSERT = """
            INSERT INTO password_entries
                (user_id, title, website, login, password_data, storage_mode)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_FIND_ALL = COLUMNS + "WHERE user_id = ?";

    private static final String SQL_FIND_ONE = COLUMNS + "WHERE id = ? AND user_id = ?";

    private static final String SQL_UPDATE = """
            UPDATE password_entries
            SET title = ?, website = ?, login = ?, password_data = ?, storage_mode = ?
            WHERE id = ? AND user_id = ?
            """;

    private static final String SQL_DELETE = "DELETE FROM password_entries WHERE id = ? AND user_id = ?";

    public PasswordEntryDAO() {
    }

    public void add(PasswordEntry entry) throws SQLException {
        JdbcExecutor.update(SQL_INSERT, entry.userId(), entry.title(), entry.website(),
                entry.login(), entry.passwordData(), entry.storageMode().name());
    }

    public List<PasswordEntry> findAllByUser(int userId) throws SQLException {
        return JdbcExecutor.query(SQL_FIND_ALL, PasswordEntryDAO::mapEntry, userId);
    }

    public Optional<PasswordEntry> findById(int userId, int entryId) throws SQLException {
        return JdbcExecutor.queryFirst(SQL_FIND_ONE, PasswordEntryDAO::mapEntry, entryId, userId);
    }

    public boolean update(PasswordEntry entry) throws SQLException {
        return JdbcExecutor.update(SQL_UPDATE, entry.title(), entry.website(), entry.login(),
                entry.passwordData(), entry.storageMode().name(), entry.id(), entry.userId()) > 0;
    }

    public boolean delete(int userId, int entryId) throws SQLException {
        return JdbcExecutor.update(SQL_DELETE, entryId, userId) > 0;
    }

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