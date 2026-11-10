package com.passwordmanager.ui;

import com.passwordmanager.config.AppConfig;
import com.passwordmanager.model.User;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.PasswordEntryService;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
public class SceneManager {
    private final StackPane rootPane = new StackPane();
    private final ModalLayer modalLayer = new ModalLayer(rootPane);
    private final ToastLayer toastLayer = new ToastLayer(rootPane);
    private final AuthService authService;

    private final PasswordEntryService entryService;
    public SceneManager(Stage stage, AuthService authService, PasswordEntryService entryService) {
        this.authService = authService;
        this.entryService = entryService;
        Scene scene = new Scene(rootPane, AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        UiFactory.applyStylesheet(scene.getStylesheets());
        stage.setScene(scene);
    }
    public void showLogin() {
        showScreen(new LoginView(authService, this).getRoot());
    }
    public void showMain(User user) {
        showScreen(new MainView(user, entryService, this).getRoot());
    }
    public void showModal(Node content) {
        modalLayer.show(content, true);
    }
    public void showBlockingModal(Node content) {
        modalLayer.show(content, false);
    }
    public void closeModal() {
        modalLayer.close();
    }
    public void showToast(String message, boolean success) {
        toastLayer.show(message, success);
    }
    private void showScreen(Node screen) {
        modalLayer.close();
        rootPane.getChildren().setAll(screen);
    }
}