package com.life.utility;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FrameTimer {

    private final AtomicInteger frames;
    private final StringProperty framesPerSecondProperty;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public FrameTimer() {
        this.frames = new AtomicInteger(0);
        this.framesPerSecondProperty = new SimpleStringProperty();
    }

    @Async
    public void tick() {
        frames.set(frames.get() + 1);
    }

    public StringProperty framesPerSecondProperty() {
        return framesPerSecondProperty;
    }

    @Scheduled(fixedRate = 1000)
    public void updateFramesPerSecond() {
        Platform.runLater(() -> framesPerSecondProperty.set(" FPS: " + frames.getAndSet(0)));
    }
}
