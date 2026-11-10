package com.passwordmanager.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
public final class MessageDialog {
    private static final double WIDTH = 400;
    private MessageDialog() {
    }
    public static Node confirm(String title, String message, String confirmText,
                               Runnable onConfirm, Runnable onCancel) {
        Button cancel = UiFactory.secondaryButton("Отмена", null, onCancel);
        Button confirm = UiFactory.dangerButton(confirmText, onConfirm);
        cancel.setCancelButton(true);
        Platform.runLater(cancel::requestFocus);
        return layout(Icon.TRASH, "dialog-icon-danger", title, message, cancel, confirm);
    }
    public static Node error(String title, String message, String buttonText, Runnable onClose) {
        Button close = UiFactory.primaryButton(buttonText, null, onClose);
        close.setDefaultButton(true);
        return layout(Icon.ALERT, "dialog-icon-danger", title, message, close);
    }
    private static VBox layout(Icon icon, String iconStyle, String title, String message, Button... buttons) {
        Label text = UiFactory.label(message, "dialog-message");
        text.setWrapText(true);
        HBox buttonRow = UiFactory.row(10, UiFactory.spacer());
        buttonRow.getChildren().addAll(buttons);
        VBox layout = new VBox(18,
                UiFactory.row(12, UiFactory.badgeIcon(icon, 22, iconStyle), UiFactory.label(title, "dialog-title")),
                text, buttonRow);
        layout.setPrefWidth(WIDTH);
        return layout;
    }
}