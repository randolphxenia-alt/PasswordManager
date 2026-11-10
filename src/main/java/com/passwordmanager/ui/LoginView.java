package com.passwordmanager.ui;

import com.passwordmanager.config.AppConfig;
import com.passwordmanager.model.User;
import com.passwordmanager.service.AppException;
import com.passwordmanager.service.AuthService;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
public class LoginView {
    private static final double CARD_WIDTH = 400;
    private final AuthService authService;
    private final SceneManager sceneManager;
    private final TextField usernameField = new TextField();
    private final PasswordInput passwordInput = new PasswordInput("Мастер-пароль");
    private final PasswordInput confirmInput = new PasswordInput("Ещё раз тот же пароль");
    private final VBox confirmField = UiFactory.field("Повторите пароль", confirmInput.getNode());
    private final Label subtitle = UiFactory.label("", "auth-subtitle");
    private final Label messageLabel = UiFactory.messageLabel();
    private final Button submitButton = UiFactory.primaryButton("", null, this::submit);
    private final Label switchHint = UiFactory.label("", "muted");
    private final Hyperlink switchLink = UiFactory.link("", this::toggleMode);
    private final StackPane root;
    private boolean registerMode;
    public LoginView(AuthService authService, SceneManager sceneManager) {
        this.authService = authService;
        this.sceneManager = sceneManager;
        this.root = buildLayout();
        applyMode(false);
        Platform.runLater(usernameField::requestFocus);
    }
    public Parent getRoot() {
        return root;
    }
    private StackPane buildLayout() {
        configureControls();
        VBox card = new VBox(22, buildHeader(), buildForm(), submitButton, buildSwitchRow());
        card.getStyleClass().add("auth-card");
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxSize(CARD_WIDTH, Region.USE_PREF_SIZE);
        StackPane layout = new StackPane(card);
        layout.getStyleClass().add("auth-background");
        return layout;
    }
    private void configureControls() {
        usernameField.setPromptText("Латиница, цифры, . _ -");
        usernameField.setOnAction(event -> submit());
        passwordInput.setOnAction(event -> submit());
        confirmInput.setOnAction(event -> submit());
        confirmField.managedProperty().bind(confirmField.visibleProperty());
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.getStyleClass().add("button-large");
    }
    private VBox buildHeader() {
        VBox header = new VBox(10,
                UiFactory.badgeIcon(Icon.LOCK, 28, "logo-badge"),
                UiFactory.label(AppConfig.APP_TITLE, "auth-title"),
                subtitle);
        header.setAlignment(Pos.CENTER);
        return header;
    }
    private VBox buildForm() {
        return new VBox(14,
                UiFactory.field("Имя пользователя", usernameField),
                UiFactory.field("Мастер-пароль", passwordInput.getNode()),
                confirmField,
                messageLabel);
    }
    private HBox buildSwitchRow() {
        HBox row = UiFactory.row(4, switchHint, switchLink);
        row.setAlignment(Pos.CENTER);
        return row;
    }
    private void applyMode(boolean register) {
        registerMode = register;
        subtitle.setText(register
                ? "Мастер-пароль будет защищён алгоритмом PBKDF2"
                : "Войдите, чтобы открыть хранилище паролей");
        submitButton.setText(register ? "Создать аккаунт" : "Войти");
        switchHint.setText(register ? "Уже есть аккаунт?" : "Нет аккаунта?");
        switchLink.setText(register ? "Войти" : "Создать");
        confirmField.setVisible(register);
        passwordInput.clear();
        confirmInput.clear();
        UiFactory.hideMessage(messageLabel);
    }
    private void toggleMode() {
        applyMode(!registerMode);
    }
    private void submit() {
        if (registerMode) {
            register();
        } else {
            login();
        }
    }
    private void login() {
        try {
            User user = authService.login(usernameField.getText(), passwordInput.getText());
            sceneManager.showMain(user);
            sceneManager.showToast("Добро пожаловать, " + user.username() + "!", true);
        } catch (AppException e) {
            UiFactory.showMessage(messageLabel, e.getMessage(), false);
            passwordInput.clear();
        }
    }
    private void register() {
        try {
            authService.register(usernameField.getText(), passwordInput.getText(), confirmInput.getText());
            applyMode(false);
            sceneManager.showToast("Аккаунт создан. Теперь войдите", true);
        } catch (AppException e) {
            UiFactory.showMessage(messageLabel, e.getMessage(), false);
        }
    }
}