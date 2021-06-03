package com.life.history;

import com.life.configuration.IterationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GenerationHistory {

    private final AtomicBoolean wasCycleDiscovered;
    private final ConcurrentLinkedQueue<Generation> incomingQueue;
    private final ArrayList<Generation> history;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GenerationHistory() {
        wasCycleDiscovered = new AtomicBoolean(false);
        incomingQueue = new ConcurrentLinkedQueue<>();
        history = new ArrayList<>();
    }

    @Async
    public void publish(Generation current) {
        if (wasCycleDiscovered.get()) {
            return;
        }
        while (history.size() > IterationSettings.MAX_HISTORY - 1) {
            history.remove(1);
        }
        if (history.contains(current)) {
            wasCycleDiscovered.set(true);
            long initialGeneration = history.get(history.indexOf(current)).generationIndex;
            long repetitionGeneration = history.get(history.size() - 1).generationIndex;
            long cyclePeriod = repetitionGeneration - initialGeneration + 1;
            LOG.info("Cycle detected from generation " + initialGeneration + " to generation " + repetitionGeneration + ".");
            LOG.info("The cycle period was " + cyclePeriod + " generations.");
            LOG.info("The unprocessed incoming queue size at the time of cycle discovery was " + incomingQueue.size() + " generations.");
            LOG.info("There were a total of " + current.generationIndex + " generations before a cycle was discovered.");
        } else {
            history.add(current);
        }
    }

    public AtomicBoolean getWasCycleDiscovered() {
        return wasCycleDiscovered;
    }
}
