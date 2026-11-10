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
public class PasswordManagerApp extends Application {
    private static final Logger log = LogManager.getLogger(PasswordManagerApp.class);
    public PasswordManagerApp() {
    }
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
    @Override
    public void stop() {
        log.info("Приложение завершено");
    }
    private void configureStage(Stage stage) {
        stage.setTitle(AppConfig.APP_TITLE);
        stage.setMinWidth(AppConfig.WINDOW_MIN_WIDTH);
        stage.setMinHeight(AppConfig.WINDOW_MIN_HEIGHT);
    }
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
    private void installUncaughtExceptionHandler(SceneManager sceneManager) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            log.error("Непредвиденная ошибка в потоке {}", thread.getName(), error);
            Platform.runLater(() -> sceneManager.showToast("Непредвиденная ошибка: " + error.getMessage(), false));
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}