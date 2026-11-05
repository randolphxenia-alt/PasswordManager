package com.passwordmanager.db;

import com.passwordmanager.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Фабрика соединений с базой данных SQLite.
 * <p>
 * Для каждого соединения включается поддержка внешних ключей
 * ({@code PRAGMA foreign_keys = ON}), иначе SQLite игнорирует
 * {@code ON DELETE CASCADE}.
 */
public final class DatabaseConnection {

    /** Текущий JDBC URL базы данных. */
    private static volatile String url = AppConfig.DATABASE_URL;

    /** Закрытый конструктор: класс содержит только статические методы. */
    private DatabaseConnection() {
    }

    /**
     * Открывает новое соединение с базой данных.
     *
     * @return открытое соединение (закрывается вызывающим кодом)
     * @throws SQLException если соединение установить не удалось
     */
    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("foreign_keys", "true");
        return DriverManager.getConnection(url, properties);
    }

    /**
     * Переключает приложение на другую базу данных (используется в unit-тестах).
     *
     * @param newUrl JDBC URL новой базы данных
     */
    public static void setUrl(String newUrl) {
        url = newUrl;
    }

    /**
     * Возвращает текущий JDBC URL.
     *
     * @return JDBC URL базы данных
     */
    public static String getUrl() {
        return url;
    }
}