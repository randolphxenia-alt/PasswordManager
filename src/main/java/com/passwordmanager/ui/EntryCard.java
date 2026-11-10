package com.passwordmanager.ui;

import com.passwordmanager.model.DecryptedEntry;
import com.passwordmanager.model.EntryData;
import com.passwordmanager.model.StorageMode;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
public class EntryCard {
    public static final double CARD_WIDTH = 300;
    private static final double VALUE_ROW_HEIGHT = 32;
    private static final String PASSWORD_MASK = "••••••••••";
    public record Actions(Consumer<DecryptedEntry> onEdit, Consumer<DecryptedEntry> onDelete,
                          Consumer<DecryptedEntry> onVerify, BiConsumer<String, String> onCopy) {
    }
    private final DecryptedEntry entry;
    private final Actions actions;
    private final Label passwordValue = UiFactory.label(PASSWORD_MASK, "value", "password-value");
    private final VBox root;
    private boolean revealed;
    public EntryCard(DecryptedEntry entry, Actions actions) {
        this.entry = entry;
        this.actions = actions;
        this.root = buildLayout();
    }
    public Node getRoot() {
        return root;
    }
    private VBox buildLayout() {
        VBox card = new VBox(16, buildHeader(), buildLoginRow(), buildPasswordRow(), buildFooter());
        card.getStyleClass().add("entry-card");
        card.setPrefWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        return card;
    }
    private HBox buildHeader() {
        EntryData data = entry.data();
        String website = data.website() == null || data.website().isBlank() ? "Сайт не указан" : data.website();
        Label title = UiFactory.label(data.title(), "card-title");
        Label site = UiFactory.label(website, "card-subtitle");
        site.setGraphic(Icon.GLOBE.create(13));
        VBox titles = new VBox(2, title, site);
        titles.setMinWidth(0);
        HBox.setHgrow(titles, Priority.ALWAYS);
        return UiFactory.row(12, UiFactory.avatar(data.title()), titles);
    }
    private VBox buildLoginRow() {
        String login = entry.data().login();
        return valueRow("Логин", UiFactory.label(login, "value"),
                UiFactory.iconButton(Icon.COPY, "Копировать логин", () -> actions.onCopy().accept(login, "Логин")));
    }
    private VBox buildPasswordRow() {
        return switch (entry.passwordState()) {
            case AVAILABLE -> valueRow("Пароль", passwordValue, buildRevealButton(),
                    UiFactory.iconButton(Icon.COPY, "Копировать пароль",
                            () -> actions.onCopy().accept(entry.data().password(), "Пароль")));
            case HASHED -> valueRow("Пароль", UiFactory.label("Хеш · не восстанавливается", "value", "muted"),
                    buildVerifyButton());
            case CORRUPTED -> valueRow("Пароль", UiFactory.label("Данные повреждены", "value", "text-danger"));
        };
    }
    private Button buildRevealButton() {
        Button button = UiFactory.iconButton(Icon.EYE, "Показать пароль", () -> { });
        button.setOnAction(event -> toggleReveal(button));
        return button;
    }
    private Button buildVerifyButton() {
        Button button = UiFactory.secondaryButton("Проверить", Icon.SHIELD, () -> actions.onVerify().accept(entry));
        button.getStyleClass().add("button-small");
        return button;
    }
    private HBox buildFooter() {
        HBox footer = UiFactory.row(4,
                buildModeBadge(entry.data().storageMode()),
                UiFactory.spacer(),
                UiFactory.iconButton(Icon.EDIT, "Изменить", () -> actions.onEdit().accept(entry)),
                UiFactory.iconButton(Icon.TRASH, "Удалить", () -> actions.onDelete().accept(entry), "icon-button-danger"));
        footer.getStyleClass().add("card-footer");
        return footer;
    }
    private static Label buildModeBadge(StorageMode mode) {
        Label badge = UiFactory.label(mode.getDisplayName(), "badge",
                mode.isReversible() ? "badge-reversible" : "badge-hash");
        badge.setTooltip(new Tooltip(mode.getDescription()));
        return badge;
    }
    private static VBox valueRow(String caption, Label value, Node... buttons) {
        value.setMaxWidth(Double.MAX_VALUE);
        value.setMinWidth(0);
        HBox.setHgrow(value, Priority.ALWAYS);
        HBox line = UiFactory.row(2, value);
        line.getChildren().addAll(buttons);
        line.setMinHeight(VALUE_ROW_HEIGHT);
        return new VBox(4, UiFactory.label(caption, "field-caption"), line);
    }
    private void toggleReveal(Button button) {
        revealed = !revealed;
        passwordValue.setText(revealed ? entry.data().password() : PASSWORD_MASK);
        button.setGraphic((revealed ? Icon.EYE_OFF : Icon.EYE).create(18));
        button.setTooltip(new Tooltip(revealed ? "Скрыть пароль" : "Показать пароль"));
    }
}