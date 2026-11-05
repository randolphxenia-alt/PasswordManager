package com.passwordmanager.config;

import com.passwordmanager.crypto.KeyVault;

/**
 * Встроенные настройки приложения.
 * Секретные ключи сюда не входят — они хранятся в
 * {@link KeyVault} в зашифрованном виде.
 */
public final class AppConfig {

    /** Путь к файлу базы данных SQLite по умолчанию. */
    public static final String DATABASE_URL = "jdbc:sqlite:password_manager.db";

    /** Путь к таблице стилей JavaFX внутри ресурсов. */
    public static final String STYLESHEET_PATH = "/style.css";

    /** Заголовок главного окна. */
    public static final String APP_TITLE = "Password Manager";

    /** Начальная ширина окна в пикселях. */
    public static final int WINDOW_WIDTH = 1040;

    /** Начальная высота окна в пикселях. */
    public static final int WINDOW_HEIGHT = 700;

    /** Минимальная ширина окна в пикселях. */
    public static final int WINDOW_MIN_WIDTH = 760;

    /** Минимальная высота окна в пикселях. */
    public static final int WINDOW_MIN_HEIGHT = 600;

    /** Закрытый конструктор: класс содержит только константы. */
    private AppConfig() {
    }
}