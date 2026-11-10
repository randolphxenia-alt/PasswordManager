package com.passwordmanager.dao;

import com.passwordmanager.db.JdbcExecutor;
import com.passwordmanager.model.UserCredentials;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAO {

    private static final String SQL_EXISTS = "SELECT 1 FROM users WHERE username = ?";

    private static final String SQL_INSERT = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

    private static final String SQL_FIND = "SELECT id, username, password_hash FROM users WHERE username = ?";

    public UserDAO() {
    }

    public boolean exists(String username) throws SQLException {
        return JdbcExecutor.queryFirst(SQL_EXISTS, resultSet -> Boolean.TRUE, username).isPresent();
    }

    public void insert(String username, String passwordHash) throws SQLException {
        JdbcExecutor.update(SQL_INSERT, username, passwordHash);
    }

    public Optional<UserCredentials> findByUsername(String username) throws SQLException {
        return JdbcExecutor.queryFirst(SQL_FIND, UserDAO::mapCredentials, username);
    }

    private static UserCredentials mapCredentials(ResultSet resultSet) throws SQLException {
        return new UserCredentials(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"));
    }
}