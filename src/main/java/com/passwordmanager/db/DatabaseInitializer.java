package com.passwordmanager.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final Logger log = LogManager.getLogger(DatabaseInitializer.class);

    private static final int SCHEMA_VERSION = 2;

    private static final String OUTDATED_SCHEMA =
            "База данных создана старой версией программы. "
                    + "Удалите файл password_manager.db и запустите программу снова.";

    private static final String SQL_USERS_TABLE_EXISTS =
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'users'";

    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                username      TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                created_at    TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private static final String CREATE_ENTRIES = """
            CREATE TABLE IF NOT EXISTS password_entries (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id       INTEGER NOT NULL,
                title         TEXT NOT NULL,
                website       TEXT,
                login         TEXT NOT NULL,
                password_data TEXT NOT NULL,
                storage_mode  TEXT NOT NULL,
                created_at    TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            ensureSchemaIsCurrent(statement);
            statement.execute(CREATE_USERS);
            statement.execute(CREATE_ENTRIES);
            statement.execute("PRAGMA user_version = " + SCHEMA_VERSION);
            log.info("Схема БД готова: {}", DatabaseConnection.getUrl());
        } catch (SQLException e) {
            log.error("Ошибка инициализации БД", e);
            throw e;
        }
    }

    private static void ensureSchemaIsCurrent(Statement statement) throws SQLException {
        if (readSchemaVersion(statement) < SCHEMA_VERSION && usersTableExists(statement)) {
            throw new SQLException(OUTDATED_SCHEMA);
        }
    }

    private static int readSchemaVersion(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private static boolean usersTableExists(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SQL_USERS_TABLE_EXISTS)) {
            return resultSet.next();
        }
    }
}