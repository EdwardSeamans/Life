package com.life.event;

import com.life.exception.FxUncaughtExceptionHandler;
import javafx.stage.Stage;

public class RuntimeStageReadyEvent {
    private final Stage stage;
    private final FxUncaughtExceptionHandler fxUncaughtExceptionHandler;

    public RuntimeStageReadyEvent(Stage stage, FxUncaughtExceptionHandler fxUncaughtExceptionHandler) {
        this.stage = stage;
        this.fxUncaughtExceptionHandler = fxUncaughtExceptionHandler;
    }

    public Stage getStage() {
        return stage;
    }

    public FxUncaughtExceptionHandler getFxUncaughtExceptionHandler() {
        return fxUncaughtExceptionHandler;
    }
}
