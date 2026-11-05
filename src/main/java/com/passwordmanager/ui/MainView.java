package com.passwordmanager.ui;

import com.passwordmanager.config.AppConfig;
import com.passwordmanager.model.DecryptedEntry;
import com.passwordmanager.model.User;
import com.passwordmanager.service.AppException;
import com.passwordmanager.service.PasswordEntryService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Главный экран: панель с поиском и кнопкой добавления, сетка карточек записей
 * и заглушка для пустого списка.
 */
public class MainView {

    /** Расстояние между карточками. */
    private static final double GRID_GAP = 18;

    /** Вошедший пользователь. */
    private final User user;

    /** Сервис записей. */
    private final PasswordEntryService entryService;

    /** Менеджер окна: модальные окна, уведомления, выход. */
    private final SceneManager sceneManager;

    /** Поле поиска. */
    private final TextField searchField = new TextField();

    /** Сетка карточек. */
    private final FlowPane cardGrid = new FlowPane(GRID_GAP, GRID_GAP);

    /** Прокручиваемая область с сеткой. */
    private final ScrollPane cardScroll = new ScrollPane(cardGrid);

    /** Счётчик записей. */
    private final Label countLabel = UiFactory.label("", "muted");

    /** Заголовок заглушки пустого списка. */
    private final Label emptyTitle = UiFactory.label("", "empty-title");

    /** Пояснение заглушки пустого списка. */
    private final Label emptyText = UiFactory.label("", "muted");

    /** Заглушка пустого списка. */
    private final VBox emptyState = new VBox(10);

    /** Корневой узел экрана. */
    private final BorderPane root;

    /**
     * Создаёт главный экран и загружает записи.
     *
     * @param user         вошедший пользователь
     * @param entryService сервис записей
     * @param sceneManager менеджер окна
     */
    public MainView(User user, PasswordEntryService entryService, SceneManager sceneManager) {
        this.user = user;
        this.entryService = entryService;
        this.sceneManager = sceneManager;
        this.root = buildLayout();
        reload();
    }

    /**
     * Возвращает корневой узел экрана.
     *
     * @return корневой узел
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Строит разметку экрана.
     *
     * @return корневой контейнер
     */
    private BorderPane buildLayout() {
        BorderPane layout = new BorderPane();
        layout.setTop(buildToolbar());
        layout.setCenter(buildContent());
        layout.getStyleClass().add("main-background");
        return layout;
    }

    /**
     * Создаёт верхнюю панель: логотип, поиск, кнопка добавления, пользователь, выход.
     *
     * @return панель
     */
    private HBox buildToolbar() {
        HBox brand = UiFactory.row(10, UiFactory.badgeIcon(Icon.LOCK, 18, "brand-badge"),
                UiFactory.label(AppConfig.APP_TITLE, "brand-title"));
        Label userChip = UiFactory.label(user.username(), "user-chip");
        userChip.setGraphic(Icon.USER.create(16));
        HBox toolbar = UiFactory.row(14, brand, UiFactory.spacer(), buildSearchBox(),
                UiFactory.primaryButton("Новая запись", Icon.PLUS, this::openAddForm),
                userChip,
                UiFactory.iconButton(Icon.LOGOUT, "Выйти из аккаунта", sceneManager::showLogin));
        toolbar.getStyleClass().add("toolbar");
        return toolbar;
    }

    /**
     * Создаёт поле поиска с иконкой; список фильтруется при вводе.
     *
     * @return поле поиска
     */
    private HBox buildSearchBox() {
        searchField.setPromptText("Поиск по названию, сайту, логину");
        searchField.textProperty().addListener((observable, oldText, newText) -> reload());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox box = UiFactory.row(6, Icon.SEARCH.create(16), searchField);
        box.getStyleClass().addAll("input-shell", "search-box");
        UiFactory.trackFocusWithin(box, searchField);
        return box;
    }

    /**
     * Создаёт основную область: заголовок раздела и сетку карточек (или заглушку).
     *
     * @return основная область
     */
    private VBox buildContent() {
        HBox heading = UiFactory.row(12, UiFactory.label("Мои пароли", "section-title"), countLabel);
        heading.setPadding(new Insets(24, 28, 8, 28));
        cardGrid.setPadding(new Insets(12, 28, 28, 28));
        cardScroll.setFitToWidth(true);
        cardScroll.getStyleClass().add("cards-scroll");
        configureEmptyState();
        StackPane body = new StackPane(cardScroll, emptyState);
        VBox.setVgrow(body, Priority.ALWAYS);
        return new VBox(heading, body);
    }

    /** Настраивает заглушку пустого списка. */
    private void configureEmptyState() {
        emptyState.getChildren().setAll(UiFactory.badgeIcon(Icon.LOCK, 34, "empty-badge"), emptyTitle, emptyText);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setMouseTransparent(true);
    }

    /** Перезагружает записи с учётом строки поиска. */
    private void reload() {
        try {
            render(entryService.findEntries(user.id(), searchField.getText()));
        } catch (AppException e) {
            render(List.of());
            sceneManager.showToast(e.getMessage(), false);
        }
    }

    /**
     * Отображает записи карточками (StreamAPI) или заглушку, если записей нет.
     *
     * @param entries записи для отображения
     */
    private void render(List<DecryptedEntry> entries) {
        cardGrid.getChildren().setAll(entries.stream().map(this::createCard).toList());
        countLabel.setText(entries.size() + " " + pluralizeEntries(entries.size()));
        boolean searching = !searchField.getText().isBlank();
        emptyTitle.setText(searching ? "Ничего не найдено" : "Здесь пока пусто");
        emptyText.setText(searching ? "Попробуйте изменить запрос" : "Нажмите «Новая запись», чтобы сохранить первый пароль");
        emptyState.setVisible(entries.isEmpty());
        cardScroll.setVisible(!entries.isEmpty());
    }

    /**
     * Создаёт карточку записи.
     *
     * @param entry запись
     * @return узел карточки
     */
    private Node createCard(DecryptedEntry entry) {
        EntryCard.Actions actions = new EntryCard.Actions(
                this::openEditForm, this::confirmDelete, this::openVerify, this::copyToClipboard);
        return new EntryCard(entry, actions).getRoot();
    }

    /** Открывает форму добавления записи. */
    private void openAddForm() {
        sceneManager.showModal(new EntryFormView("Новая запись", null, data -> {
            entryService.addEntry(user.id(), data);
            onChanged("Запись «" + data.title() + "» добавлена");
        }, sceneManager::closeModal).getRoot());
    }

    /**
     * Открывает форму изменения записи.
     *
     * @param entry изменяемая запись
     */
    private void openEditForm(DecryptedEntry entry) {
        sceneManager.showModal(new EntryFormView("Изменение записи", entry, data -> {
            entryService.updateEntry(user.id(), entry.id(), data);
            onChanged("Изменения сохранены");
        }, sceneManager::closeModal).getRoot());
    }

    /**
     * Запрашивает подтверждение удаления.
     *
     * @param entry удаляемая запись
     */
    private void confirmDelete(DecryptedEntry entry) {
        sceneManager.showModal(MessageDialog.confirm("Удалить запись?",
                "Запись «" + entry.data().title() + "» будет удалена без возможности восстановления.",
                "Удалить", () -> deleteEntry(entry), sceneManager::closeModal));
    }

    /**
     * Удаляет запись.
     *
     * @param entry удаляемая запись
     */
    private void deleteEntry(DecryptedEntry entry) {
        try {
            entryService.deleteEntry(user.id(), entry.id());
            onChanged("Запись «" + entry.data().title() + "» удалена");
        } catch (AppException e) {
            sceneManager.closeModal();
            sceneManager.showToast(e.getMessage(), false);
        }
    }

    /**
     * Открывает окно проверки хеш-пароля.
     *
     * @param entry запись
     */
    private void openVerify(DecryptedEntry entry) {
        sceneManager.showModal(new VerifyPasswordView(entry,
                candidate -> entryService.verifyPassword(user.id(), entry.id(), candidate),
                sceneManager::closeModal).getRoot());
    }

    /**
     * Копирует значение в буфер обмена.
     *
     * @param value значение
     * @param what  что копируется (для уведомления)
     */
    private void copyToClipboard(String value, String what) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard.getSystemClipboard().setContent(content);
        sceneManager.showToast(what + " скопирован в буфер обмена", true);
    }

    /**
     * Закрывает окно, показывает уведомление и обновляет список после изменения данных.
     *
     * @param message текст уведомления
     */
    private void onChanged(String message) {
        sceneManager.closeModal();
        sceneManager.showToast(message, true);
        reload();
    }

    /**
     * Подбирает форму слова «запись» для числа.
     *
     * @param count количество
     * @return «запись», «записи» или «записей»
     */
    static String pluralizeEntries(int count) {
        int lastTwo = count % 100;
        int last = count % 10;
        if (lastTwo >= 11 && lastTwo <= 14) {
            return "записей";
        }
        if (last == 1) {
            return "запись";
        }
        return last >= 2 && last <= 4 ? "записи" : "записей";
    }
}