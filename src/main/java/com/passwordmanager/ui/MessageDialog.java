package com.passwordmanager.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Типовые окна-сообщения в оформлении приложения: подтверждение действия и сообщение об ошибке.
 */
public final class MessageDialog {

    /** Ширина окна. */
    private static final double WIDTH = 400;

    /** Закрытый конструктор: класс содержит только статические методы. */
    private MessageDialog() {
    }

    /**
     * Создаёт окно подтверждения опасного действия.
     *
     * @param title       заголовок
     * @param message     пояснение
     * @param confirmText текст кнопки подтверждения
     * @param onConfirm   действие при подтверждении
     * @param onCancel    действие при отмене
     * @return содержимое окна
     */
    public static Node confirm(String title, String message, String confirmText,
                               Runnable onConfirm, Runnable onCancel) {
        Button cancel = UiFactory.secondaryButton("Отмена", null, onCancel);
        Button confirm = UiFactory.dangerButton(confirmText, onConfirm);
        cancel.setCancelButton(true);
        Platform.runLater(cancel::requestFocus);
        return layout(Icon.TRASH, "dialog-icon-danger", title, message, cancel, confirm);
    }

    /**
     * Создаёт окно сообщения об ошибке.
     *
     * @param title      заголовок
     * @param message    пояснение
     * @param buttonText текст кнопки
     * @param onClose    действие при нажатии кнопки
     * @return содержимое окна
     */
    public static Node error(String title, String message, String buttonText, Runnable onClose) {
        Button close = UiFactory.primaryButton(buttonText, null, onClose);
        close.setDefaultButton(true);
        return layout(Icon.ALERT, "dialog-icon-danger", title, message, close);
    }

    /**
     * Собирает окно: иконка, заголовок, текст и кнопки справа внизу.
     *
     * @param icon      иконка
     * @param iconStyle CSS-класс значка иконки
     * @param title     заголовок
     * @param message   пояснение
     * @param buttons   кнопки
     * @return содержимое окна
     */
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