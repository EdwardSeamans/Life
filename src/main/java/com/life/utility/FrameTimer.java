package com.life.utility;

import com.life.executor.PipelineExecutor;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FrameTimer {

    private final AtomicInteger frames;
    private final StringProperty framesPerSecondProperty;
    private final AtomicBoolean isPaused;
    private boolean firstFrame = true;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public FrameTimer(PipelineExecutor pipelineExecutor) {
        this.isPaused = pipelineExecutor.isPaused();
        this.frames = new AtomicInteger(0);
        this.framesPerSecondProperty = new SimpleStringProperty();
    }

    public void tick() {
        frames.set(frames.get() + 1);
        if (firstFrame) {
            firstFrame = false;
            isPaused.set(true);
        }
    }

    public StringProperty framesPerSecondProperty() {
        return framesPerSecondProperty;
    }

    @Scheduled(fixedRate = 1000)
    public void updateFramesPerSecond() {
        if (isPaused.get()) {
            frames.set(0);
        }
        Platform.runLater(() -> framesPerSecondProperty.set(" FPS: " + frames.getAndSet(0)));
    }
}
