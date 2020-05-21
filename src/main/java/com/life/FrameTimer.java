package com.life;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FrameTimer {
    private AtomicInteger frames;
    private final static Logger LOG = LoggerFactory.getLogger(FrameTimer.class);
    private boolean wasCycleDiscovered;

    public FrameTimer() {
        frames = new AtomicInteger(0);
        wasCycleDiscovered = false;
    }

    public void tick() {
        frames.set(frames.get() +1);
    }

    public void setWasCycleDiscovered() {
        wasCycleDiscovered = true;
    }

    @Scheduled(fixedRate = 5000, initialDelay = 1000)
    public void logFramesPerSecond() {
        if (!wasCycleDiscovered) {
            LOG.info("Frames per second: " + frames.getAndSet(0)/5);
        }
    }
}
