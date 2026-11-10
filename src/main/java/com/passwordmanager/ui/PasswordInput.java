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
public final class PasswordInput {
    private final PasswordField hiddenField = new PasswordField();
    private final TextField visibleField = new TextField();
    private final Button toggleButton;
    private final HBox root;
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
    public Node getNode() {
        return root;
    }
    public String getText() {
        return hiddenField.getText();
    }
    public void setText(String text) {
        hiddenField.setText(text);
    }
    public void requestFocus() {
        (visibleField.isVisible() ? visibleField : hiddenField).requestFocus();
    }
    public void clear() {
        hiddenField.clear();
    }
    public void setPromptText(String prompt) {
        hiddenField.setPromptText(prompt);
        visibleField.setPromptText(prompt);
    }
    public void setOnAction(EventHandler<ActionEvent> handler) {
        hiddenField.setOnAction(handler);
        visibleField.setOnAction(handler);
    }
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
    private void toggleVisibility() {
        setRevealed(!visibleField.isVisible());
    }
}