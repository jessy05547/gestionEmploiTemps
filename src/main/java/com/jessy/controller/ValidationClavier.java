package com.jessy.controller;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class ValidationClavier {
    private ValidationClavier() {
    }

    public static void installer(Node racine, Runnable validation) {
        javafx.event.EventHandler<KeyEvent> filtre = event -> {
            if (event.getCode() == KeyCode.ENTER && !(event.getTarget() instanceof TextArea)) {
                event.consume();
                validation.run();
            }
        };
        racine.sceneProperty().addListener((obs, ancienneScene, nouvelleScene) -> {
            if (nouvelleScene != null) {
                nouvelleScene.addEventFilter(KeyEvent.KEY_PRESSED, filtre);
            }
        });
        if (racine.getScene() != null) {
            racine.getScene().addEventFilter(KeyEvent.KEY_PRESSED, filtre);
        }
    }
}
