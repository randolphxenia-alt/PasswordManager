package com.passwordmanager.config;

import com.passwordmanager.crypto.KeyVault;

public final class AppConfig {
    public static final String DATABASE_URL = "jdbc:sqlite:password_manager.db";
    public static final String STYLESHEET_PATH = "/style.css";
    public static final String APP_TITLE = "Password Manager";
    public static final int WINDOW_WIDTH = 1040;
    public static final int WINDOW_HEIGHT = 700;
    public static final int WINDOW_MIN_WIDTH = 760;
    public static final int WINDOW_MIN_HEIGHT = 600;
    private AppConfig() {
    }
}