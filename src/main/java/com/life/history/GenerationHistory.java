package com.life.history;

import com.life.configuration.IterationSettings;
import com.life.render.FrameTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GenerationHistory {

    private final AtomicBoolean wasCycleDiscovered;
    private final ConcurrentLinkedQueue<Generation> incomingQueue;
    private final ArrayList<Generation> history;
    private final FrameTimer frameTimer;
    private long droppedGenerations;

    private final static Logger LOG = LoggerFactory.getLogger(GenerationHistory.class);

    public GenerationHistory(FrameTimer frameTimer) {
        this.frameTimer = frameTimer;
        wasCycleDiscovered = new AtomicBoolean(false);
        incomingQueue = new ConcurrentLinkedQueue<>();
        history = new ArrayList<>();
        droppedGenerations = 0;
    }

    public void publishToQueue(boolean[] currentCells) {
        incomingQueue.add(new Generation(currentCells));
    }

    @Scheduled(fixedDelay = IterationSettings.TARGET_FRAME_INTERVAL, initialDelay = 1000)
    public void processFrame() {
        if (wasCycleDiscovered.get() || incomingQueue.isEmpty()) {
            incomingQueue.clear();
            return;
        }
        while(history.size() > IterationSettings.MAX_HISTORY) {
            history.remove(0);
            droppedGenerations++;
        }
        Generation current = incomingQueue.remove();
        if (history.contains(current)) {
            long initialGeneration = history.indexOf(current) + 1 + droppedGenerations;
            history.add(current);
            long repetitionGeneration = history.size() + droppedGenerations;
            long cyclePeriod = repetitionGeneration - initialGeneration;
            LOG.info("Cycle detected from generation " + initialGeneration + " to generation " + repetitionGeneration + ".");
            LOG.info("The cycle period was " + cyclePeriod + " generations.");
            wasCycleDiscovered.set(true);
            history.clear();
            frameTimer.setWasCycleDiscovered();
        }
        else {
            history.add(current);
        }
    }
}
