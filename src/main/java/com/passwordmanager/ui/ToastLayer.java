package com.passwordmanager.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Всплывающие уведомления («тосты») внизу окна: появляются, висят пару секунд и исчезают.
 */
class ToastLayer {

    /** Длительность появления и исчезновения. */
    private static final Duration FADE = Duration.millis(220);

    /** Сколько уведомление остаётся на экране. */
    private static final Duration VISIBLE = Duration.seconds(2.6);

    /** Контейнер, поверх которого показываются уведомления. */
    private final StackPane host;

    /** Текущее уведомление или {@code null}. */
    private Label current;

    /**
     * Создаёт слой уведомлений.
     *
     * @param host корневой контейнер сцены
     */
    ToastLayer(StackPane host) {
        this.host = host;
    }

    /**
     * Показывает уведомление (предыдущее убирается сразу).
     *
     * @param message текст
     * @param success {@code true} — успех, {@code false} — ошибка
     */
    void show(String message, boolean success) {
        host.getChildren().remove(current);
        current = createToast(message, success);
        host.getChildren().add(current);
        animate(current);
    }

    /**
     * Создаёт узел уведомления.
     *
     * @param message текст
     * @param success вид уведомления
     * @return узел уведомления
     */
    private static Label createToast(String message, boolean success) {
        Label toast = new Label(message, (success ? Icon.CHECK : Icon.ALERT).create(18));
        toast.getStyleClass().addAll("toast", success ? "toast-success" : "toast-error");
        toast.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        toast.setMouseTransparent(true);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 28, 0));
        return toast;
    }

    /**
     * Анимирует появление, паузу и исчезновение уведомления.
     *
     * @param toast уведомление
     */
    private void animate(Label toast) {
        FadeTransition fadeIn = new FadeTransition(FADE, toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        FadeTransition fadeOut = new FadeTransition(FADE, toast);
        fadeOut.setToValue(0);
        SequentialTransition sequence = new SequentialTransition(fadeIn, new PauseTransition(VISIBLE), fadeOut);
        sequence.setOnFinished(event -> host.getChildren().remove(toast));
        sequence.play();
    }
}