package com.life.event;

import javafx.stage.Stage;

public class RuntimeStageReadyEvent {
    private final Stage stage;

    public RuntimeStageReadyEvent(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
