package com.passwordmanager.ui;

import com.passwordmanager.model.DecryptedEntry;
import com.passwordmanager.model.EntryData;
import com.passwordmanager.model.PasswordState;
import com.passwordmanager.model.StorageMode;
import com.passwordmanager.service.AppException;
import com.passwordmanager.service.InputValidator;
import com.passwordmanager.service.PasswordGenerator;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Форма добавления и изменения записи (содержимое модального окна).
 * Ошибки ввода показываются прямо в форме, окно при этом не закрывается.
 */
public class EntryFormView {

    /**
     * Обработчик сохранения формы.
     */
    @FunctionalInterface
    public interface SaveHandler {

        /**
         * Сохраняет введённые данные.
         *
         * @param data данные формы
         * @throws AppException если данные некорректны или не сохранились
         */
        void save(EntryData data) throws AppException;
    }

    /** Ширина формы. */
    private static final double FORM_WIDTH = 480;

    /** Поле названия. */
    private final TextField titleField = new TextField();

    /** Поле сайта. */
    private final TextField websiteField = new TextField();

    /** Поле логина. */
    private final TextField loginField = new TextField();

    /** Поле пароля. */
    private final PasswordInput passwordInput = new PasswordInput("Введите или сгенерируйте");

    /** Выбор способа защиты. */
    private final ComboBox<StorageMode> modeBox = new ComboBox<>();

    /** Пояснение к выбранному способу защиты. */
    private final Label modeHint = UiFactory.label("", "hint");

    /** Сообщение об ошибке. */
    private final Label messageLabel = UiFactory.messageLabel();

    /** Обработчик сохранения. */
    private final SaveHandler saveHandler;

    /** Действие при отмене. */
    private final Runnable onCancel;

    /** Корневой узел формы. */
    private final VBox root;

    /**
     * Создаёт форму.
     *
     * @param heading     заголовок формы
     * @param existing    изменяемая запись или {@code null} для новой
     * @param saveHandler обработчик сохранения
     * @param onCancel    действие при отмене
     */
    public EntryFormView(String heading, DecryptedEntry existing, SaveHandler saveHandler, Runnable onCancel) {
        this.saveHandler = saveHandler;
        this.onCancel = onCancel;
        configureControls();
        Optional.ofNullable(existing).ifPresent(this::fill);
        this.root = buildLayout(heading);
        Platform.runLater(titleField::requestFocus);
    }

    /**
     * Возвращает узел формы.
     *
     * @return корневой узел
     */
    public Node getRoot() {
        return root;
    }

    /** Настраивает поля и список способов защиты. */
    private void configureControls() {
        titleField.setPromptText("Например, GitHub");
        websiteField.setPromptText("github.com");
        loginField.setPromptText("Логин или e-mail");
        modeBox.getItems().setAll(StorageMode.values());
        modeBox.valueProperty().addListener((observable, oldMode, newMode) -> updateModeHint(newMode));
        modeBox.setValue(StorageMode.FEISTEL);
        modeBox.setMaxWidth(Double.MAX_VALUE);
        modeHint.setWrapText(true);
    }

    /**
     * Заполняет форму данными изменяемой записи.
     * Если пароль недоступен (хеш), поле остаётся пустым и означает «не менять».
     *
     * @param entry изменяемая запись
     */
    private void fill(DecryptedEntry entry) {
        EntryData data = entry.data();
        titleField.setText(data.title());
        websiteField.setText(data.website());
        loginField.setText(data.login());
        modeBox.setValue(data.storageMode());
        if (entry.passwordState() == PasswordState.AVAILABLE) {
            passwordInput.setText(data.password());
        } else {
            passwordInput.setPromptText("Оставьте пустым, чтобы не менять");
        }
    }

    /**
     * Строит разметку формы.
     *
     * @param heading заголовок
     * @return корневой контейнер
     */
    private VBox buildLayout(String heading) {
        VBox layout = new VBox(20, buildHeader(heading), buildFields(), messageLabel, buildButtons());
        layout.setPrefWidth(FORM_WIDTH);
        return layout;
    }

    /**
     * Создаёт заголовок с кнопкой закрытия.
     *
     * @param heading текст заголовка
     * @return строка заголовка
     */
    private HBox buildHeader(String heading) {
        return UiFactory.row(8, UiFactory.label(heading, "dialog-title"), UiFactory.spacer(),
                UiFactory.iconButton(Icon.CLOSE, "Закрыть", onCancel));
    }

    /**
     * Размещает поля в сетке: название и сайт в одной строке, остальные — на всю ширину.
     *
     * @return сетка полей
     */
    private GridPane buildFields() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        VBox title = UiFactory.field("Название", titleField);
        VBox website = UiFactory.field("Сайт", websiteField);
        GridPane.setHgrow(title, Priority.ALWAYS);
        GridPane.setHgrow(website, Priority.ALWAYS);
        grid.add(title, 0, 0);
        grid.add(website, 1, 0);
        grid.add(UiFactory.field("Логин", loginField), 0, 1, 2, 1);
        grid.add(UiFactory.field("Пароль", buildPasswordRow()), 0, 2, 2, 1);
        grid.add(new VBox(6, UiFactory.field("Способ защиты пароля", modeBox), modeHint), 0, 3, 2, 1);
        return grid;
    }

    /**
     * Создаёт строку пароля с кнопкой генерации.
     *
     * @return строка пароля
     */
    private HBox buildPasswordRow() {
        Node input = passwordInput.getNode();
        HBox.setHgrow(input, Priority.ALWAYS);
        Button generate = UiFactory.secondaryButton("Сгенерировать", Icon.REFRESH, this::generatePassword);
        return UiFactory.row(8, input, generate);
    }

    /**
     * Создаёт кнопки «Отмена» и «Сохранить».
     *
     * @return строка кнопок
     */
    private HBox buildButtons() {
        Button cancel = UiFactory.secondaryButton("Отмена", null, onCancel);
        Button save = UiFactory.primaryButton("Сохранить", Icon.CHECK, this::save);
        cancel.setCancelButton(true);
        save.setDefaultButton(true);
        return UiFactory.row(10, UiFactory.spacer(), cancel, save);
    }

    /**
     * Показывает пояснение к выбранному способу защиты.
     *
     * @param mode выбранный способ
     */
    private void updateModeHint(StorageMode mode) {
        modeHint.setText(mode == null ? "" : mode.getDescription());
    }

    /** Подставляет в поле пароля новый случайный пароль и показывает его. */
    private void generatePassword() {
        passwordInput.setText(PasswordGenerator.generate());
        passwordInput.setRevealed(true);
    }

    /** Передаёт данные формы обработчику; при ошибке показывает её в форме. */
    private void save() {
        try {
            saveHandler.save(readForm());
        } catch (AppException e) {
            UiFactory.showMessage(messageLabel, e.getMessage(), false);
        }
    }

    /**
     * Считывает значения полей.
     *
     * @return данные формы
     */
    private EntryData readForm() {
        return new EntryData(
                InputValidator.normalize(titleField.getText()),
                InputValidator.normalize(websiteField.getText()),
                InputValidator.normalize(loginField.getText()),
                passwordInput.getText(),
                modeBox.getValue());
    }
}