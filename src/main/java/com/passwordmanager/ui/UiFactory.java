package com.passwordmanager.ui;

import com.passwordmanager.config.AppConfig;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
public final class UiFactory {
    private static final PseudoClass FOCUS_WITHIN = PseudoClass.getPseudoClass("focus-within");
    private static final String ERROR_STYLE = "message-error";
    private static final String SUCCESS_STYLE = "message-success";
    private static final double BUTTON_ICON_SIZE = 16;
    private static final Logger log = LogManager.getLogger(UiFactory.class);
    private UiFactory() {
    }
    public static void applyStylesheet(ObservableList<String> stylesheets) {
        Optional.ofNullable(UiFactory.class.getResource(AppConfig.STYLESHEET_PATH))
                .map(URL::toExternalForm)
                .ifPresentOrElse(stylesheets::add,
                        () -> log.warn("Файл стилей {} не найден", AppConfig.STYLESHEET_PATH));
    }
    public static Label label(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }
    public static Button primaryButton(String text, Icon icon, Runnable action) {
        return textButton(text, icon, action, "button-primary");
    }
    public static Button secondaryButton(String text, Icon icon, Runnable action) {
        return textButton(text, icon, action, "button-secondary");
    }
    public static Button dangerButton(String text, Runnable action) {
        return textButton(text, Icon.TRASH, action, "button-danger");
    }
    public static Button iconButton(Icon icon, String tooltip, Runnable action, String... styles) {
        Button button = new Button();
        button.setGraphic(icon.create(18));
        button.setTooltip(new Tooltip(tooltip));
        button.getStyleClass().add("icon-button");
        button.getStyleClass().addAll(styles);
        button.setOnAction(event -> action.run());
        return button;
    }
    public static Hyperlink link(String text, Runnable action) {
        Hyperlink link = new Hyperlink(text);
        link.setOnAction(event -> action.run());
        return link;
    }
    public static VBox field(String caption, Node control) {
        VBox field = new VBox(6, label(caption, "field-caption"), control);
        field.setFillWidth(true);
        return field;
    }
    public static Label messageLabel() {
        Label label = label("", "message");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.managedProperty().bind(label.visibleProperty());
        label.setVisible(false);
        return label;
    }
    public static void showMessage(Label label, String message, boolean success) {
        label.getStyleClass().removeAll(ERROR_STYLE, SUCCESS_STYLE);
        label.getStyleClass().add(success ? SUCCESS_STYLE : ERROR_STYLE);
        label.setGraphic((success ? Icon.CHECK : Icon.ALERT).create(16));
        label.setText(message);
        label.setVisible(true);
    }
    public static void hideMessage(Label label) {
        label.setVisible(false);
    }
    public static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
    public static StackPane badgeIcon(Icon icon, double size, String styleClass) {
        StackPane badge = new StackPane(icon.create(size));
        badge.getStyleClass().add(styleClass);
        badge.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return badge;
    }
    public static StackPane avatar(String title) {
        String letter = title == null || title.isBlank() ? "?" : title.strip().substring(0, 1);
        StackPane avatar = new StackPane(label(letter.toUpperCase(Locale.ROOT), "avatar-letter"));
        avatar.getStyleClass().add("avatar");
        return avatar;
    }
    public static HBox row(double spacing, Node... children) {
        HBox row = new HBox(spacing, children);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
    public static void trackFocusWithin(Node shell, Node... inputs) {
        Arrays.stream(inputs).forEach(input -> input.focusedProperty().addListener((observable, was, now) ->
                shell.pseudoClassStateChanged(FOCUS_WITHIN,
                        Arrays.stream(inputs).anyMatch(Node::isFocused))));
    }
    private static Button textButton(String text, Icon icon, Runnable action, String styleClass) {
        Button button = new Button(text);
        Optional.ofNullable(icon).ifPresent(value -> button.setGraphic(value.create(BUTTON_ICON_SIZE)));
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }
}