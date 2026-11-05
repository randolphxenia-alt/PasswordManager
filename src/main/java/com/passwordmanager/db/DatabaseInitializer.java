package com.passwordmanager.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Создание схемы базы данных при первом запуске и проверка её версии.
 * Версия схемы хранится в {@code PRAGMA user_version}.
 */
public final class DatabaseInitializer {

    /** Логгер класса. */
    private static final Logger log = LogManager.getLogger(DatabaseInitializer.class);

    /** Текущая версия схемы БД. */
    private static final int SCHEMA_VERSION = 2;

    /** Сообщение о базе данных, созданной старой версией программы. */
    private static final String OUTDATED_SCHEMA =
            "База данных создана старой версией программы. "
                    + "Удалите файл password_manager.db и запустите программу снова.";

    /** Запрос проверки существования таблицы пользователей. */
    private static final String SQL_USERS_TABLE_EXISTS =
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'users'";

    /** DDL таблицы пользователей. */
    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                username      TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                created_at    TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    /** DDL таблицы записей менеджера паролей. */
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

    /** Закрытый конструктор: класс содержит только статические методы. */
    private DatabaseInitializer() {
    }

    /**
     * Создаёт таблицы, если они ещё не существуют, и проверяет версию схемы.
     *
     * @throws SQLException если схему создать не удалось или БД создана старой версией программы
     */
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

    /**
     * Проверяет, что БД не была создана предыдущей версией программы с другой схемой.
     *
     * @param statement открытый запрос
     * @throws SQLException если схема устарела
     */
    private static void ensureSchemaIsCurrent(Statement statement) throws SQLException {
        if (readSchemaVersion(statement) < SCHEMA_VERSION && usersTableExists(statement)) {
            throw new SQLException(OUTDATED_SCHEMA);
        }
    }

    /**
     * Читает версию схемы из {@code PRAGMA user_version}.
     *
     * @param statement открытый запрос
     * @return версия схемы (0 для новой или старой БД)
     * @throws SQLException при ошибке чтения
     */
    private static int readSchemaVersion(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    /**
     * Проверяет, есть ли в БД таблица пользователей.
     *
     * @param statement открытый запрос
     * @return {@code true}, если таблица существует
     * @throws SQLException при ошибке запроса
     */
    private static boolean usersTableExists(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SQL_USERS_TABLE_EXISTS)) {
            return resultSet.next();
        }
    }
}