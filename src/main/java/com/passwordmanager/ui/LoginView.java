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

/**
 * Экран входа и регистрации: карточка по центру окна.
 * Режим «Регистрация» добавляет поле повтора пароля; формат хранения мастер-пароля
 * пользователь не выбирает — всегда используется PBKDF2 с солью.
 */
public class LoginView {

    /** Ширина карточки входа. */
    private static final double CARD_WIDTH = 400;

    /** Сервис регистрации и входа. */
    private final AuthService authService;

    /** Менеджер окна для перехода после входа и уведомлений. */
    private final SceneManager sceneManager;

    /** Поле имени пользователя. */
    private final TextField usernameField = new TextField();

    /** Поле мастер-пароля. */
    private final PasswordInput passwordInput = new PasswordInput("Мастер-пароль");

    /** Поле повтора мастер-пароля (только при регистрации). */
    private final PasswordInput confirmInput = new PasswordInput("Ещё раз тот же пароль");

    /** Блок «Повторите пароль» целиком. */
    private final VBox confirmField = UiFactory.field("Повторите пароль", confirmInput.getNode());

    /** Подзаголовок карточки. */
    private final Label subtitle = UiFactory.label("", "auth-subtitle");

    /** Сообщение об ошибке. */
    private final Label messageLabel = UiFactory.messageLabel();

    /** Кнопка «Войти» / «Создать аккаунт». */
    private final Button submitButton = UiFactory.primaryButton("", null, this::submit);

    /** Пояснение перед ссылкой переключения режима. */
    private final Label switchHint = UiFactory.label("", "muted");

    /** Ссылка переключения режима. */
    private final Hyperlink switchLink = UiFactory.link("", this::toggleMode);

    /** Корневой узел экрана. */
    private final StackPane root;

    /** Текущий режим: {@code true} — регистрация. */
    private boolean registerMode;

    /**
     * Создаёт экран входа.
     *
     * @param authService  сервис регистрации и входа
     * @param sceneManager менеджер окна
     */
    public LoginView(AuthService authService, SceneManager sceneManager) {
        this.authService = authService;
        this.sceneManager = sceneManager;
        this.root = buildLayout();
        applyMode(false);
        Platform.runLater(usernameField::requestFocus);
    }

    /**
     * Возвращает корневой узел экрана.
     *
     * @return корневой узел
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Строит разметку экрана.
     *
     * @return корневой контейнер
     */
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

    /** Настраивает поля, кнопку и обработку Enter. */
    private void configureControls() {
        usernameField.setPromptText("Латиница, цифры, . _ -");
        usernameField.setOnAction(event -> submit());
        passwordInput.setOnAction(event -> submit());
        confirmInput.setOnAction(event -> submit());
        confirmField.managedProperty().bind(confirmField.visibleProperty());
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.getStyleClass().add("button-large");
    }

    /**
     * Создаёт шапку карточки: логотип, название, подзаголовок.
     *
     * @return шапка
     */
    private VBox buildHeader() {
        VBox header = new VBox(10,
                UiFactory.badgeIcon(Icon.LOCK, 28, "logo-badge"),
                UiFactory.label(AppConfig.APP_TITLE, "auth-title"),
                subtitle);
        header.setAlignment(Pos.CENTER);
        return header;
    }

    /**
     * Создаёт поля формы.
     *
     * @return контейнер с полями
     */
    private VBox buildForm() {
        return new VBox(14,
                UiFactory.field("Имя пользователя", usernameField),
                UiFactory.field("Мастер-пароль", passwordInput.getNode()),
                confirmField,
                messageLabel);
    }

    /**
     * Создаёт строку «Нет аккаунта? Создать».
     *
     * @return строка переключения режима
     */
    private HBox buildSwitchRow() {
        HBox row = UiFactory.row(4, switchHint, switchLink);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    /**
     * Переключает экран между входом и регистрацией.
     *
     * @param register {@code true} — режим регистрации
     */
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

    /** Меняет режим на противоположный. */
    private void toggleMode() {
        applyMode(!registerMode);
    }

    /** Отправляет форму в зависимости от режима. */
    private void submit() {
        if (registerMode) {
            register();
        } else {
            login();
        }
    }

    /** Выполняет вход и открывает хранилище. */
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

    /** Регистрирует пользователя и переключает экран на вход. */
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