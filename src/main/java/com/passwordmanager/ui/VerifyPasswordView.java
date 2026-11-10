package com.passwordmanager.ui;

import com.passwordmanager.model.DecryptedEntry;
import com.passwordmanager.service.AppException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
public class VerifyPasswordView {
    @FunctionalInterface
    public interface PasswordVerifier {
        boolean verify(String candidate) throws AppException;
    }
    private static final double WIDTH = 420;
    private final PasswordInput passwordInput = new PasswordInput("Пароль для проверки");
    private final Label resultLabel = UiFactory.messageLabel();
    private final PasswordVerifier verifier;
    private final VBox root;
    public VerifyPasswordView(DecryptedEntry entry, PasswordVerifier verifier, Runnable onClose) {
        this.verifier = verifier;
        passwordInput.setOnAction(event -> check());
        this.root = buildLayout(entry, onClose);
        Platform.runLater(passwordInput::requestFocus);
    }
    public Node getRoot() {
        return root;
    }
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
    private HBox buildButtons(Runnable onClose) {
        Button close = UiFactory.secondaryButton("Закрыть", null, onClose);
        Button check = UiFactory.primaryButton("Проверить", Icon.SHIELD, this::check);
        close.setCancelButton(true);
        return UiFactory.row(10, UiFactory.spacer(), close, check);
    }
    private void check() {
        try {
            boolean matches = verifier.verify(passwordInput.getText());
            UiFactory.showMessage(resultLabel, matches ? "Пароль совпадает" : "Пароль не совпадает", matches);
        } catch (AppException e) {
            UiFactory.showMessage(resultLabel, e.getMessage(), false);
        }
    }
}