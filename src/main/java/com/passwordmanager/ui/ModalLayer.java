package com.passwordmanager.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
class ModalLayer {
    private static final Duration ANIMATION = Duration.millis(160);
    private final StackPane host;
    private StackPane backdrop;
    ModalLayer(StackPane host) {
        this.host = host;
    }
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
    void close() {
        if (backdrop != null) {
            host.getChildren().remove(backdrop);
            backdrop = null;
        }
    }
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