package com.life.event;

import javafx.stage.Stage;

public class RenderingStageReadyEvent {

    private final Stage stage;

    public RenderingStageReadyEvent(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
