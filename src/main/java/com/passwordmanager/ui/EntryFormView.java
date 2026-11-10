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
public class EntryFormView {
    @FunctionalInterface
    public interface SaveHandler {
        void save(EntryData data) throws AppException;
    }
    private static final double FORM_WIDTH = 480;
    private final TextField titleField = new TextField();
    private final TextField websiteField = new TextField();
    private final TextField loginField = new TextField();
    private final PasswordInput passwordInput = new PasswordInput("Введите или сгенерируйте");
    private final ComboBox<StorageMode> modeBox = new ComboBox<>();
    private final Label modeHint = UiFactory.label("", "hint");
    private final Label messageLabel = UiFactory.messageLabel();
    private final SaveHandler saveHandler;
    private final Runnable onCancel;
    private final VBox root;
    public EntryFormView(String heading, DecryptedEntry existing, SaveHandler saveHandler, Runnable onCancel) {
        this.saveHandler = saveHandler;
        this.onCancel = onCancel;
        configureControls();
        Optional.ofNullable(existing).ifPresent(this::fill);
        this.root = buildLayout(heading);
        Platform.runLater(titleField::requestFocus);
    }
    public Node getRoot() {
        return root;
    }
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
    private VBox buildLayout(String heading) {
        VBox layout = new VBox(20, buildHeader(heading), buildFields(), messageLabel, buildButtons());
        layout.setPrefWidth(FORM_WIDTH);
        return layout;
    }
    private HBox buildHeader(String heading) {
        return UiFactory.row(8, UiFactory.label(heading, "dialog-title"), UiFactory.spacer(),
                UiFactory.iconButton(Icon.CLOSE, "Закрыть", onCancel));
    }
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
    private HBox buildPasswordRow() {
        Node input = passwordInput.getNode();
        HBox.setHgrow(input, Priority.ALWAYS);
        Button generate = UiFactory.secondaryButton("Сгенерировать", Icon.REFRESH, this::generatePassword);
        return UiFactory.row(8, input, generate);
    }
    private HBox buildButtons() {
        Button cancel = UiFactory.secondaryButton("Отмена", null, onCancel);
        Button save = UiFactory.primaryButton("Сохранить", Icon.CHECK, this::save);
        cancel.setCancelButton(true);
        save.setDefaultButton(true);
        return UiFactory.row(10, UiFactory.spacer(), cancel, save);
    }
    private void updateModeHint(StorageMode mode) {
        modeHint.setText(mode == null ? "" : mode.getDescription());
    }
    private void generatePassword() {
        passwordInput.setText(PasswordGenerator.generate());
        passwordInput.setRevealed(true);
    }
    private void save() {
        try {
            saveHandler.save(readForm());
        } catch (AppException e) {
            UiFactory.showMessage(messageLabel, e.getMessage(), false);
        }
    }
    private EntryData readForm() {
        return new EntryData(
                InputValidator.normalize(titleField.getText()),
                InputValidator.normalize(websiteField.getText()),
                InputValidator.normalize(loginField.getText()),
                passwordInput.getText(),
                modeBox.getValue());
    }
}