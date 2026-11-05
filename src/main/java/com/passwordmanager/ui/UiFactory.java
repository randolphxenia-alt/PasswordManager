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

/**
 * Фабрика типовых элементов интерфейса в едином стиле: кнопки, подписи,
 * поля с заголовками, сообщения об ошибках, аватары.
 * Все экраны строятся из этих элементов, поэтому выглядят согласованно.
 */
public final class UiFactory {

    /** Псевдокласс «фокус внутри составного поля». */
    private static final PseudoClass FOCUS_WITHIN = PseudoClass.getPseudoClass("focus-within");

    /** CSS-класс сообщения об ошибке. */
    private static final String ERROR_STYLE = "message-error";

    /** CSS-класс сообщения об успехе. */
    private static final String SUCCESS_STYLE = "message-success";

    /** Размер иконок внутри кнопок. */
    private static final double BUTTON_ICON_SIZE = 16;

    /** Логгер класса. */
    private static final Logger log = LogManager.getLogger(UiFactory.class);

    /** Закрытый конструктор: класс содержит только статические методы. */
    private UiFactory() {
    }

    /**
     * Подключает таблицу стилей приложения. Если файл не найден,
     * интерфейс остаётся работоспособным со стандартным оформлением.
     *
     * @param stylesheets список таблиц стилей сцены или узла
     */
    public static void applyStylesheet(ObservableList<String> stylesheets) {
        Optional.ofNullable(UiFactory.class.getResource(AppConfig.STYLESHEET_PATH))
                .map(URL::toExternalForm)
                .ifPresentOrElse(stylesheets::add,
                        () -> log.warn("Файл стилей {} не найден", AppConfig.STYLESHEET_PATH));
    }

    /**
     * Создаёт надпись с CSS-классами.
     *
     * @param text         текст
     * @param styleClasses CSS-классы
     * @return надпись
     */
    public static Label label(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    /**
     * Создаёт основную (залитую акцентным цветом) кнопку.
     *
     * @param text   текст
     * @param icon   иконка или {@code null}
     * @param action действие при нажатии
     * @return кнопка
     */
    public static Button primaryButton(String text, Icon icon, Runnable action) {
        return textButton(text, icon, action, "button-primary");
    }

    /**
     * Создаёт второстепенную (контурную) кнопку.
     *
     * @param text   текст
     * @param icon   иконка или {@code null}
     * @param action действие при нажатии
     * @return кнопка
     */
    public static Button secondaryButton(String text, Icon icon, Runnable action) {
        return textButton(text, icon, action, "button-secondary");
    }

    /**
     * Создаёт кнопку опасного действия (удаление).
     *
     * @param text   текст
     * @param action действие при нажатии
     * @return кнопка
     */
    public static Button dangerButton(String text, Runnable action) {
        return textButton(text, Icon.TRASH, action, "button-danger");
    }

    /**
     * Создаёт кнопку-иконку с всплывающей подсказкой.
     *
     * @param icon    иконка
     * @param tooltip подсказка
     * @param action  действие при нажатии
     * @param styles  дополнительные CSS-классы
     * @return кнопка
     */
    public static Button iconButton(Icon icon, String tooltip, Runnable action, String... styles) {
        Button button = new Button();
        button.setGraphic(icon.create(18));
        button.setTooltip(new Tooltip(tooltip));
        button.getStyleClass().add("icon-button");
        button.getStyleClass().addAll(styles);
        button.setOnAction(event -> action.run());
        return button;
    }

    /**
     * Создаёт текстовую ссылку-действие.
     *
     * @param text   текст
     * @param action действие при нажатии
     * @return ссылка
     */
    public static Hyperlink link(String text, Runnable action) {
        Hyperlink link = new Hyperlink(text);
        link.setOnAction(event -> action.run());
        return link;
    }

    /**
     * Оформляет поле ввода с заголовком над ним.
     *
     * @param caption заголовок
     * @param control поле ввода
     * @return контейнер «заголовок + поле»
     */
    public static VBox field(String caption, Node control) {
        VBox field = new VBox(6, label(caption, "field-caption"), control);
        field.setFillWidth(true);
        return field;
    }

    /**
     * Создаёт скрытую строку сообщения (появляется при вызове {@link #showMessage}).
     *
     * @return строка сообщения
     */
    public static Label messageLabel() {
        Label label = label("", "message");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.managedProperty().bind(label.visibleProperty());
        label.setVisible(false);
        return label;
    }

    /**
     * Показывает сообщение об успехе или ошибке.
     *
     * @param label   строка сообщения
     * @param message текст
     * @param success {@code true} — успех, {@code false} — ошибка
     */
    public static void showMessage(Label label, String message, boolean success) {
        label.getStyleClass().removeAll(ERROR_STYLE, SUCCESS_STYLE);
        label.getStyleClass().add(success ? SUCCESS_STYLE : ERROR_STYLE);
        label.setGraphic((success ? Icon.CHECK : Icon.ALERT).create(16));
        label.setText(message);
        label.setVisible(true);
    }

    /**
     * Скрывает строку сообщения.
     *
     * @param label строка сообщения
     */
    public static void hideMessage(Label label) {
        label.setVisible(false);
    }

    /**
     * Создаёт растягивающийся промежуток для горизонтальной строки.
     *
     * @return пустой растягивающийся элемент
     */
    public static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /**
     * Создаёт квадратный значок с иконкой (логотип, иконка диалога).
     *
     * @param icon       иконка
     * @param size       размер иконки
     * @param styleClass CSS-класс значка
     * @return значок
     */
    public static StackPane badgeIcon(Icon icon, double size, String styleClass) {
        StackPane badge = new StackPane(icon.create(size));
        badge.getStyleClass().add(styleClass);
        badge.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return badge;
    }

    /**
     * Создаёт аватар записи — первую букву названия в цветном квадрате.
     *
     * @param title название записи
     * @return аватар
     */
    public static StackPane avatar(String title) {
        String letter = title == null || title.isBlank() ? "?" : title.strip().substring(0, 1);
        StackPane avatar = new StackPane(label(letter.toUpperCase(Locale.ROOT), "avatar-letter"));
        avatar.getStyleClass().add("avatar");
        return avatar;
    }

    /**
     * Создаёт строку элементов с выравниванием по центру по вертикали.
     *
     * @param spacing  расстояние между элементами
     * @param children элементы
     * @return горизонтальный контейнер
     */
    public static HBox row(double spacing, Node... children) {
        HBox row = new HBox(spacing, children);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Подсвечивает составное поле, когда фокус находится в любом из его элементов
     * (в JavaFX CSS нет {@code :focus-within}, поэтому псевдокласс ставится вручную).
     *
     * @param shell  внешний контейнер поля
     * @param inputs элементы ввода внутри контейнера
     */
    public static void trackFocusWithin(Node shell, Node... inputs) {
        Arrays.stream(inputs).forEach(input -> input.focusedProperty().addListener((observable, was, now) ->
                shell.pseudoClassStateChanged(FOCUS_WITHIN,
                        Arrays.stream(inputs).anyMatch(Node::isFocused))));
    }

    /**
     * Создаёт кнопку с текстом и иконкой.
     *
     * @param text       текст
     * @param icon       иконка или {@code null}
     * @param action     действие при нажатии
     * @param styleClass CSS-класс вида кнопки
     * @return кнопка
     */
    private static Button textButton(String text, Icon icon, Runnable action, String styleClass) {
        Button button = new Button(text);
        Optional.ofNullable(icon).ifPresent(value -> button.setGraphic(value.create(BUTTON_ICON_SIZE)));
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }
}