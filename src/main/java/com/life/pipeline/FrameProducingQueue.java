package com.life.pipeline;

import com.life.contract.Pipeline;
import com.life.payload.Generation;
import com.life.payload.Frame;
import com.life.executor.PipelineExecutor;
import com.life.utility.FrameTimer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.life.configuration.IterationSettings.BLACK;
import static com.life.configuration.IterationSettings.BYTES_PER_PIXEL;
import static com.life.configuration.IterationSettings.COLUMNS;
import static com.life.configuration.IterationSettings.GREEN;
import static com.life.configuration.IterationSettings.ROWS;
import static com.life.configuration.IterationSettings.SCALING_FACTOR;

@Component
public class FrameProducingQueue extends SynchronousQueue<Generation> implements Pipeline {

    private final Runnable action;
    private final AtomicBoolean stop;
    private final StringProperty actionNameProperty;

    private final RenderingControllerQueue renderingControllerQueue;
    private final FrameTimer frameTimer;

    private static final byte[] LIVE_COLOR = GREEN;
    private static final byte[] DEAD_COLOR = BLACK;
    private final byte[] LIVE_COLOR_CHUNK = new byte[BYTES_PER_PIXEL * SCALING_FACTOR];
    private final byte[] DEAD_COLOR_CHUNK = new byte[BYTES_PER_PIXEL * SCALING_FACTOR];

    private static final String ACTION_NAME_PROPERTY_STRING = "Produce Frames";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public FrameProducingQueue(RenderingControllerQueue renderingControllerQueue, FrameTimer frameTimer, PipelineExecutor pipelineExecutor) {
        super(true);
        this.renderingControllerQueue = renderingControllerQueue;
        this.frameTimer = frameTimer;
        for (int scalingFactorRepetition = 0; scalingFactorRepetition < SCALING_FACTOR; scalingFactorRepetition++) {
            System.arraycopy(LIVE_COLOR, 0, LIVE_COLOR_CHUNK, scalingFactorRepetition * BYTES_PER_PIXEL, LIVE_COLOR.length);
            System.arraycopy(DEAD_COLOR, 0, DEAD_COLOR_CHUNK, scalingFactorRepetition * BYTES_PER_PIXEL, DEAD_COLOR.length);
        }
        this.action = this::produceFrames;
        this.actionNameProperty = new SimpleStringProperty(ACTION_NAME_PROPERTY_STRING);
        this.stop = pipelineExecutor.getStop();
        pipelineExecutor.registerAndRun(this);
    }

    public void produceFrames() {
        Generation generation;
        boolean[] cells;
        byte[] cellColorChunk;
        byte[] rowChunk = new byte[COLUMNS * SCALING_FACTOR * BYTES_PER_PIXEL];
        byte[] buffer = new byte[BYTES_PER_PIXEL * COLUMNS * SCALING_FACTOR * ROWS * SCALING_FACTOR];
        int bufferPosition;
        int bufferIncrement = COLUMNS * SCALING_FACTOR * BYTES_PER_PIXEL;

        while (!stop.get()) {
            try {
                generation = take();
            } catch (InterruptedException e) {
                LOG.error("The produceFrame thread was interrupted.", e);
                stop.set(true);
                return;
            }
            cells = generation.generationArray;
            bufferPosition = 0;

            for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
                for (int columnIndex = 0; columnIndex < COLUMNS; columnIndex++) {
                    if (cells[COLUMNS * rowIndex + columnIndex]) {
                        cellColorChunk = LIVE_COLOR_CHUNK;
                    } else {
                        cellColorChunk = DEAD_COLOR_CHUNK;
                    }
                    System.arraycopy(cellColorChunk, 0, rowChunk, columnIndex * SCALING_FACTOR * BYTES_PER_PIXEL, cellColorChunk.length);
                }
                for (int scalingRepetitions = 0; scalingRepetitions < SCALING_FACTOR; scalingRepetitions++) {
                    System.arraycopy(rowChunk, 0, buffer, bufferPosition, rowChunk.length);
                    bufferPosition += bufferIncrement;
                }
            }
            try {
                renderingControllerQueue.put(new Frame(buffer));
                frameTimer.tick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
