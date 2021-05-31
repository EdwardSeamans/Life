package com.life.history;

import com.life.configuration.IterationSettings;
import com.life.contract.Resettable;
import com.life.render.FrameTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
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
    private long droppedGenerations;
    private long totalGenerations;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GenerationHistory(FrameTimer frameTimer) {
        this.frameTimer = frameTimer;
        wasCycleDiscovered = new AtomicBoolean(false);
        incomingQueue = new ConcurrentLinkedQueue<>();
        history = new ArrayList<>();
        droppedGenerations = 0;
        totalGenerations = 0;
    }

    public void publishToQueue(boolean[] currentCells) {
        if (wasCycleDiscovered.get()) {
            return;
        }
        totalGenerations++;
        incomingQueue.add(new Generation(currentCells));
    }

    @Scheduled(fixedDelay = IterationSettings.TARGET_FRAME_INTERVAL, initialDelay = 1000)
    public void processFrame() {
        if (wasCycleDiscovered.get() || incomingQueue.isEmpty()) {
            return;
        }
        while(history.size() > IterationSettings.MAX_HISTORY) {
            history.remove(1);
            droppedGenerations++;
        }
        Generation current = incomingQueue.remove();
        if (history.contains(current)) {
            wasCycleDiscovered.set(true);
            long initialGeneration = history.indexOf(current) + 1 + droppedGenerations;
            history.add(current);
            long repetitionGeneration = history.size() + droppedGenerations;
            long cyclePeriod = repetitionGeneration - initialGeneration;
            LOG.info("Cycle detected from generation " + initialGeneration + " to generation " + repetitionGeneration + ".");
            LOG.info("The cycle period was " + cyclePeriod + " generations.");
            LOG.info("The unprocessed incoming queue size at the time of cycle discovery was " + incomingQueue.size() + " generations.");
            LOG.info("There were a total of " + totalGenerations + " generations before a cycle was discovered.");
            LOG.info("There were a total of " + droppedGenerations + " dropped generations.");
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
        droppedGenerations= 0;
    }
}
