package com.life.utility;

import com.life.pipeline.RenderingControllerQueue;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
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
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private boolean wasCycleDiscovered;

    private final ImageView imageView;

    public FrameTimer(RenderingControllerQueue renderingControllerQueue) {
        this.frames = new AtomicInteger(0);
        this.wasCycleDiscovered = false;
        this.imageView = renderingControllerQueue.getImageView();
    }

    @Async
    public void tick() {
        frames.set(frames.get() + 1);
        if (!imageView.isFocused()) {
            Platform.runLater(imageView::requestFocus);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void logFramesPerSecond() {
        if (!wasCycleDiscovered) {
            LOG.info("FPS: " + frames.getAndSet(0));
        }
    }
}
