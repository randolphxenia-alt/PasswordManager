package com.passwordmanager.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * Поле ввода пароля с кнопкой «показать / скрыть».
 * Внутри два поля (скрытое и открытое) с общим текстом; видно только одно из них.
 */
public final class PasswordInput {

    /** Поле со скрытыми символами. */
    private final PasswordField hiddenField = new PasswordField();

    /** Поле с видимым текстом. */
    private final TextField visibleField = new TextField();

    /** Кнопка переключения видимости. */
    private final Button toggleButton;

    /** Внешний контейнер поля. */
    private final HBox root;

    /**
     * Создаёт поле ввода пароля.
     *
     * @param prompt подсказка внутри пустого поля
     */
    public PasswordInput(String prompt) {
        hiddenField.textProperty().bindBidirectional(visibleField.textProperty());
        setPromptText(prompt);
        visibleField.setVisible(false);
        toggleButton = UiFactory.iconButton(Icon.EYE, "Показать пароль", this::toggleVisibility);
        toggleButton.setFocusTraversable(false);
        StackPane fields = new StackPane(hiddenField, visibleField);
        HBox.setHgrow(fields, Priority.ALWAYS);
        root = UiFactory.row(0, fields, toggleButton);
        root.getStyleClass().add("input-shell");
        UiFactory.trackFocusWithin(root, hiddenField, visibleField);
    }

    /**
     * Возвращает узел для размещения на экране.
     *
     * @return корневой узел поля
     */
    public Node getNode() {
        return root;
    }

    /**
     * Возвращает введённый пароль.
     *
     * @return текст поля
     */
    public String getText() {
        return hiddenField.getText();
    }

    /**
     * Устанавливает пароль.
     *
     * @param text новый текст
     */
    public void setText(String text) {
        hiddenField.setText(text);
    }

    /** Переводит фокус ввода в поле пароля. */
    public void requestFocus() {
        (visibleField.isVisible() ? visibleField : hiddenField).requestFocus();
    }

    /** Очищает поле. */
    public void clear() {
        hiddenField.clear();
    }

    /**
     * Устанавливает подсказку внутри пустого поля.
     *
     * @param prompt текст подсказки
     */
    public void setPromptText(String prompt) {
        hiddenField.setPromptText(prompt);
        visibleField.setPromptText(prompt);
    }

    /**
     * Задаёт действие при нажатии Enter в поле.
     *
     * @param handler обработчик
     */
    public void setOnAction(EventHandler<ActionEvent> handler) {
        hiddenField.setOnAction(handler);
        visibleField.setOnAction(handler);
    }

    /**
     * Показывает или скрывает символы пароля.
     *
     * @param revealed {@code true} — показать
     */
    public void setRevealed(boolean revealed) {
        boolean hadFocus = hiddenField.isFocused() || visibleField.isFocused();
        visibleField.setVisible(revealed);
        hiddenField.setVisible(!revealed);
        toggleButton.setGraphic((revealed ? Icon.EYE_OFF : Icon.EYE).create(18));
        toggleButton.setTooltip(new Tooltip(revealed ? "Скрыть пароль" : "Показать пароль"));
        if (hadFocus) {
            TextField active = revealed ? visibleField : hiddenField;
            active.requestFocus();
            active.end();
        }
    }

    /** Переключает видимость символов пароля. */
    private void toggleVisibility() {
        setRevealed(!visibleField.isVisible());
    }
}