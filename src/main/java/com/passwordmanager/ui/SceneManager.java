package com.passwordmanager.ui;

import com.passwordmanager.config.AppConfig;
import com.passwordmanager.model.User;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.PasswordEntryService;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Управление содержимым главного окна: переключение экранов,
 * модальные окна и всплывающие уведомления.
 */
public class SceneManager {

    /** Корневой контейнер сцены: экран, модальные окна и уведомления — слоями друг над другом. */
    private final StackPane rootPane = new StackPane();

    /** Слой модальных окон. */
    private final ModalLayer modalLayer = new ModalLayer(rootPane);

    /** Слой уведомлений. */
    private final ToastLayer toastLayer = new ToastLayer(rootPane);

    /** Сервис регистрации и входа. */
    private final AuthService authService;

    /** Сервис записей. */
    private final PasswordEntryService entryService;

    /**
     * Создаёт менеджер и устанавливает сцену в окно.
     *
     * @param stage        главное окно
     * @param authService  сервис регистрации и входа
     * @param entryService сервис записей
     */
    public SceneManager(Stage stage, AuthService authService, PasswordEntryService entryService) {
        this.authService = authService;
        this.entryService = entryService;
        Scene scene = new Scene(rootPane, AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        UiFactory.applyStylesheet(scene.getStylesheets());
        stage.setScene(scene);
    }

    /** Показывает экран входа и регистрации. */
    public void showLogin() {
        showScreen(new LoginView(authService, this).getRoot());
    }

    /**
     * Показывает экран записей пользователя.
     *
     * @param user вошедший пользователь
     */
    public void showMain(User user) {
        showScreen(new MainView(user, entryService, this).getRoot());
    }

    /**
     * Показывает модальное окно, которое можно закрыть клавишей Esc или щелчком по фону.
     *
     * @param content содержимое окна
     */
    public void showModal(Node content) {
        modalLayer.show(content, true);
    }

    /**
     * Показывает модальное окно, которое закрывается только кнопкой внутри него.
     *
     * @param content содержимое окна
     */
    public void showBlockingModal(Node content) {
        modalLayer.show(content, false);
    }

    /** Закрывает текущее модальное окно. */
    public void closeModal() {
        modalLayer.close();
    }

    /**
     * Показывает всплывающее уведомление.
     *
     * @param message текст
     * @param success {@code true} — успех, {@code false} — ошибка
     */
    public void showToast(String message, boolean success) {
        toastLayer.show(message, success);
    }

    /**
     * Заменяет текущий экран (модальные окна и уведомления закрываются).
     *
     * @param screen корневой узел нового экрана
     */
    private void showScreen(Node screen) {
        modalLayer.close();
        rootPane.getChildren().setAll(screen);
    }
}