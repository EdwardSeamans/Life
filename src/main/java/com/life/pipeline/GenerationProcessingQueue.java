package com.life.pipeline;

import com.life.contract.Pipeline;
import com.life.executor.PipelineExecutor;
import com.life.payload.Generation;
import com.life.utility.GenerationHistory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GenerationProcessingQueue extends SynchronousQueue<Generation> implements Pipeline {

    private final FrameProducingQueue frameProducingQueue;
    private final GenerationHistory generationHistory;

    private final Runnable action;
    private final AtomicBoolean stop;
    private final StringProperty actionNameProperty;

    private static final String ACTION_NAME_PROPERTY_STRING = "Process Generations";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GenerationProcessingQueue(FrameProducingQueue frameProducingQueue, PipelineExecutor pipelineExecutor, GenerationHistory generationHistory) {
        super(true);
        this.generationHistory = generationHistory;
        this.frameProducingQueue = frameProducingQueue;
        this.action = this::processGenerations;
        this.actionNameProperty = new SimpleStringProperty(ACTION_NAME_PROPERTY_STRING);
        this.stop = pipelineExecutor.getStop();
        pipelineExecutor.registerAndRun(this);
    }

    public void processGenerations() {
        Generation current = null;
        Optional<Integer> generationIndex;
        while (!stop.get()) {
            try {
                current = take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            generationIndex = generationHistory.indexOf(current);
            if (generationIndex.isPresent()) {
                cycle(generationIndex.get());
                return;
            } else {
                try {
                    frameProducingQueue.put(current);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                generationHistory.add(current);
            }
        }
    }

    private void cycle(int cycleStartIndex) {
        int cycleEndIndex = generationHistory.getLastIndex();
        logCycleInformation(cycleStartIndex, cycleEndIndex);

        while (!stop.get()) {
            for (int index = cycleStartIndex; index <= cycleEndIndex; index++) {
                try {
                    frameProducingQueue.put(generationHistory.unpackFrameAtIndex(index));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void logCycleInformation(int cycleStartIndex, int cycleEndIndex) {
        LOG.info("Cycle detected from generation " + cycleStartIndex + " to generation " + cycleEndIndex + ".");
        LOG.info("The cycle period was " + (cycleEndIndex - cycleStartIndex) + " generations.");
        Runtime runtime = Runtime.getRuntime();
        LOG.info("Total: " + runtime.totalMemory() + " Max: " + runtime.maxMemory() + " Free: " + runtime.freeMemory()+ " Processors: " + runtime.availableProcessors() + " History: " + generationHistory.size());
        LOG.info("Total: " + runtime.totalMemory() + " Max: " + runtime.maxMemory() + " Free: " + runtime.freeMemory()+ " Processors: " + runtime.availableProcessors() + " History: " + generationHistory.size());
    }

    @Override
    public Runnable getTask() {
        return action;
    }

    @Override
    public StringProperty actionNameProperty() {
        return actionNameProperty;
    }
}