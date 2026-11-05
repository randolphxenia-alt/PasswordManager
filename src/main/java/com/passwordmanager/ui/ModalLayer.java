package com.passwordmanager.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Модальные окна внутри главного окна приложения: затемнённый фон и карточка по центру.
 * Заменяет системные диалоги, которые выбиваются из оформления программы.
 */
class ModalLayer {

    /** Длительность анимации появления. */
    private static final Duration ANIMATION = Duration.millis(160);

    /** Контейнер, поверх содержимого которого показываются окна. */
    private final StackPane host;

    /** Текущий затемнённый фон с окном или {@code null}. */
    private StackPane backdrop;

    /**
     * Создаёт слой модальных окон.
     *
     * @param host корневой контейнер сцены
     */
    ModalLayer(StackPane host) {
        this.host = host;
    }

    /**
     * Показывает модальное окно (предыдущее закрывается).
     *
     * @param content     содержимое окна
     * @param dismissible можно ли закрыть окно клавишей Esc или щелчком по фону
     */
    void show(Node content, boolean dismissible) {
        close();
        StackPane card = new StackPane(content);
        card.getStyleClass().add("modal-card");
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        backdrop = new StackPane(card);
        backdrop.getStyleClass().add("modal-backdrop");
        if (dismissible) {
            installDismissHandlers(backdrop);
        }
        host.getChildren().add(backdrop);
        animateAppearance(backdrop, card);
    }

    /** Закрывает текущее модальное окно, если оно открыто. */
    void close() {
        if (backdrop != null) {
            host.getChildren().remove(backdrop);
            backdrop = null;
        }
    }

    /**
     * Позволяет закрыть окно клавишей Esc или щелчком по затемнённому фону.
     *
     * @param layer затемнённый фон
     */
    private void installDismissHandlers(StackPane layer) {
        layer.setOnMouseClicked(event -> {
            if (event.getTarget() == layer) {
                close();
            }
        });
        layer.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });
    }

    /**
     * Плавно показывает фон и слегка увеличивает карточку.
     *
     * @param layer затемнённый фон
     * @param card  карточка окна
     */
    private static void animateAppearance(Node layer, Node card) {
        FadeTransition fade = new FadeTransition(ANIMATION, layer);
        fade.setFromValue(0);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(ANIMATION, card);
        scale.setFromX(0.96);
        scale.setFromY(0.96);
        scale.setToX(1);
        scale.setToY(1);
        new ParallelTransition(fade, scale).play();
    }
}