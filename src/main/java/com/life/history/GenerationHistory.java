package com.life.history;

import com.life.configuration.IterationSettings;
import com.life.contract.Resettable;
import com.life.render.FrameTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GenerationHistory implements Resettable {

    private final AtomicBoolean wasCycleDiscovered;
    private final ConcurrentLinkedQueue<Generation> incomingQueue;
    private final ArrayList<Generation> history;
    private final FrameTimer frameTimer;
    private final LongProperty droppedGenerations;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GenerationHistory(FrameTimer frameTimer) {
        this.frameTimer = frameTimer;
        wasCycleDiscovered = new AtomicBoolean(false);
        incomingQueue = new ConcurrentLinkedQueue<>();
        history = new ArrayList<>();
        droppedGenerations = new SimpleLongProperty(0);
    }

    public void publishToQueue(boolean[] currentCells) {
        if (wasCycleDiscovered.get()) {
            return;
        }
        incomingQueue.add(new Generation(currentCells));
    }

    @Scheduled(fixedDelay = IterationSettings.TARGET_FRAME_INTERVAL, initialDelay = 1000)
    public void processFrame() {
        if (wasCycleDiscovered.get() || incomingQueue.isEmpty()) {
            return;
        }
        while(history.size() > IterationSettings.MAX_HISTORY) {
            history.remove(0);
            droppedGenerations.add(1L);
        }
        Generation current = incomingQueue.remove();
        if (history.contains(current)) {
            long initialGeneration = history.indexOf(current) + 1 + droppedGenerations.get();
            history.add(current);
            long repetitionGeneration = history.size() + droppedGenerations.get();
            long cyclePeriod = repetitionGeneration - initialGeneration;
            LOG.info("Cycle detected from generation " + initialGeneration + " to generation " + repetitionGeneration + ".");
            LOG.info("The cycle period was " + cyclePeriod + " generations.");
            LOG.info("The unprocessed incoming queue size at the time of cycle discovery was " + incomingQueue.size() + " generations.");
            wasCycleDiscovered.set(true);
            history.clear();
            frameTimer.setWasCycleDiscovered();
        }
        else {
            history.add(current);
        }
    }

    @Override
    public void reset() {
        wasCycleDiscovered.set(false);
        incomingQueue.clear();
        history.clear();
        droppedGenerations.set(0);
    }
}
