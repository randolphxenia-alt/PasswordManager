package com.passwordmanager.db;

import com.passwordmanager.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static volatile String url = AppConfig.DATABASE_URL;


    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("foreign_keys", "true");
        return DriverManager.getConnection(url, properties);
    }

    public static void setUrl(String newUrl) {
        url = newUrl;
    }

    public static String getUrl() {
        return url;
    }
}