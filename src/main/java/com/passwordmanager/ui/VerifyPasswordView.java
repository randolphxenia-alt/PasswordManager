package com.passwordmanager.ui;

import com.passwordmanager.model.DecryptedEntry;
import com.passwordmanager.service.AppException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Окно проверки пароля, сохранённого необратимым хешем (MD5 или хеш с солью):
 * пользователь вводит пароль, приложение сообщает, совпадает ли он с сохранённым.
 */
public class VerifyPasswordView {

    /**
     * Проверка введённого пароля.
     */
    @FunctionalInterface
    public interface PasswordVerifier {

        /**
         * Проверяет пароль.
         *
         * @param candidate введённый пароль
         * @return {@code true}, если пароль совпадает с сохранённым
         * @throws AppException если проверить пароль не удалось
         */
        boolean verify(String candidate) throws AppException;
    }

    /** Ширина окна. */
    private static final double WIDTH = 420;

    /** Поле ввода пароля. */
    private final PasswordInput passwordInput = new PasswordInput("Пароль для проверки");

    /** Результат проверки. */
    private final Label resultLabel = UiFactory.messageLabel();

    /** Проверка пароля. */
    private final PasswordVerifier verifier;

    /** Корневой узел окна. */
    private final VBox root;

    /**
     * Создаёт окно проверки.
     *
     * @param entry    проверяемая запись
     * @param verifier проверка пароля
     * @param onClose  действие при закрытии
     */
    public VerifyPasswordView(DecryptedEntry entry, PasswordVerifier verifier, Runnable onClose) {
        this.verifier = verifier;
        passwordInput.setOnAction(event -> check());
        this.root = buildLayout(entry, onClose);
        Platform.runLater(passwordInput::requestFocus);
    }

    /**
     * Возвращает узел окна.
     *
     * @return корневой узел
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Строит разметку окна.
     *
     * @param entry   проверяемая запись
     * @param onClose действие при закрытии
     * @return корневой контейнер
     */
    private VBox buildLayout(DecryptedEntry entry, Runnable onClose) {
        Label description = UiFactory.label("Пароль записи «" + entry.data().title() + "» сохранён способом «"
                + entry.data().storageMode().getDisplayName()
                + "». Его нельзя посмотреть, но можно проверить, совпадает ли он с введённым.", "dialog-message");
        description.setWrapText(true);
        VBox layout = new VBox(18,
                UiFactory.row(12, UiFactory.badgeIcon(Icon.SHIELD, 22, "dialog-icon"),
                        UiFactory.label("Проверка пароля", "dialog-title")),
                description, passwordInput.getNode(), resultLabel, buildButtons(onClose));
        layout.setPrefWidth(WIDTH);
        return layout;
    }

    /**
     * Создаёт кнопки «Закрыть» и «Проверить».
     *
     * @param onClose действие при закрытии
     * @return строка кнопок
     */
    private HBox buildButtons(Runnable onClose) {
        Button close = UiFactory.secondaryButton("Закрыть", null, onClose);
        Button check = UiFactory.primaryButton("Проверить", Icon.SHIELD, this::check);
        close.setCancelButton(true);
        return UiFactory.row(10, UiFactory.spacer(), close, check);
    }

    /** Проверяет введённый пароль и показывает результат. */
    private void check() {
        try {
            boolean matches = verifier.verify(passwordInput.getText());
            UiFactory.showMessage(resultLabel, matches ? "Пароль совпадает" : "Пароль не совпадает", matches);
        } catch (AppException e) {
            UiFactory.showMessage(resultLabel, e.getMessage(), false);
        }
    }
}