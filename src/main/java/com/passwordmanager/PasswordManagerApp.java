package com.passwordmanager;

import com.passwordmanager.config.AppConfig;
import com.passwordmanager.db.DatabaseInitializer;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.PasswordEntryService;
import com.passwordmanager.ui.MessageDialog;
import com.passwordmanager.ui.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

/**
 * JavaFX-приложение «Менеджер паролей».
 */
public class PasswordManagerApp extends Application {

    /** Логгер класса. */
    private static final Logger log = LogManager.getLogger(PasswordManagerApp.class);

    /** Создаёт приложение (экземпляр создаётся платформой JavaFX). */
    public PasswordManagerApp() {
    }

    /**
     * Настраивает окно, инициализирует БД и показывает экран входа.
     * Если БД недоступна, пользователь видит сообщение, и приложение корректно закрывается.
     *
     * @param stage главное окно, созданное JavaFX
     */
    @Override
    public void start(Stage stage) {
        configureStage(stage);
        SceneManager sceneManager = new SceneManager(stage, new AuthService(), new PasswordEntryService());
        installUncaughtExceptionHandler(sceneManager);
        if (initializeDatabase(sceneManager)) {
            sceneManager.showLogin();
        }
        stage.show();
        log.info("Приложение запущено");
    }

    /**
     * Вызывается JavaFX при закрытии приложения.
     */
    @Override
    public void stop() {
        log.info("Приложение завершено");
    }

    /**
     * Задаёт заголовок и минимальный размер окна.
     *
     * @param stage главное окно
     */
    private void configureStage(Stage stage) {
        stage.setTitle(AppConfig.APP_TITLE);
        stage.setMinWidth(AppConfig.WINDOW_MIN_WIDTH);
        stage.setMinHeight(AppConfig.WINDOW_MIN_HEIGHT);
    }

    /**
     * Создаёт таблицы БД; при ошибке показывает окно с сообщением и кнопкой выхода.
     *
     * @param sceneManager менеджер окна
     * @return {@code true}, если БД готова к работе
     */
    private boolean initializeDatabase(SceneManager sceneManager) {
        try {
            DatabaseInitializer.initialize();
            return true;
        } catch (SQLException e) {
            sceneManager.showBlockingModal(MessageDialog.error("Не удалось открыть базу данных",
                    e.getMessage(),
                    "Закрыть программу", Platform::exit));
            return false;
        }
    }

    /**
     * Устанавливает обработчик непредвиденных исключений: ошибка записывается в лог
     * и показывается пользователю уведомлением, а приложение продолжает работу.
     *
     * @param sceneManager менеджер окна для показа уведомления
     */
    private void installUncaughtExceptionHandler(SceneManager sceneManager) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            log.error("Непредвиденная ошибка в потоке {}", thread.getName(), error);
            Platform.runLater(() -> sceneManager.showToast("Непредвиденная ошибка: " + error.getMessage(), false));
        });
    }

    /**
     * Запускает JavaFX-приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch(args);
    }
}