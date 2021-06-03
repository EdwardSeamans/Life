package com.life.service;

import com.life.generate.GenerationQueue;
import com.life.history.Generation;
import com.life.render.Frame;
import com.life.render.FrameQueue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static com.life.configuration.IterationSettings.BLACK;
import static com.life.configuration.IterationSettings.BYTES_PER_PIXEL;
import static com.life.configuration.IterationSettings.COLUMNS;
import static com.life.configuration.IterationSettings.GREEN;
import static com.life.configuration.IterationSettings.ROWS;
import static com.life.configuration.IterationSettings.SCALING_FACTOR;

@Service
public class FrameRenderingService {

    private final GenerationQueue generationQueue;
    private final FrameQueue frameQueue;

    private static final byte[] LIVE_COLOR = GREEN;
    private static final byte[] DEAD_COLOR = BLACK;
    private final byte[] LIVE_COLOR_CHUNK = new byte[BYTES_PER_PIXEL * SCALING_FACTOR];
    private final byte[] DEAD_COLOR_CHUNK = new byte[BYTES_PER_PIXEL * SCALING_FACTOR];

    public FrameRenderingService(GenerationQueue generationQueue, FrameQueue frameQueue) {
        this.generationQueue = generationQueue;
        this.frameQueue = frameQueue;
        for (int scalingFactorRepitition = 0; scalingFactorRepitition < SCALING_FACTOR; scalingFactorRepitition++) {
            System.arraycopy(LIVE_COLOR, 0, LIVE_COLOR_CHUNK, scalingFactorRepitition * BYTES_PER_PIXEL, LIVE_COLOR.length);
            System.arraycopy(DEAD_COLOR, 0, DEAD_COLOR_CHUNK, scalingFactorRepitition * BYTES_PER_PIXEL, DEAD_COLOR.length);
        }
    }

    @Scheduled(fixedRate = 5, initialDelay = 9000)
    private void checkThroughput() {
        if (!frameQueue.needsFrame() || generationQueue.isEmpty()) {
            return;
        }
        renderFrameFromNextGeneration();
    }

    public void renderFrameFromNextGeneration() {
        Generation generation = generationQueue.getNextGeneration();
        boolean[] cells = generation.generationArray;
        byte[] cellColorChunk;
        byte[] rowChunk = new byte[COLUMNS * SCALING_FACTOR * BYTES_PER_PIXEL];

        byte[] buffer = new byte[BYTES_PER_PIXEL * COLUMNS * SCALING_FACTOR * ROWS * SCALING_FACTOR];
        int bufferPosition = 0;
        int bufferIncrement = COLUMNS * SCALING_FACTOR * BYTES_PER_PIXEL;

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
                System.arraycopy(rowChunk, 0, buffer, bufferPosition , rowChunk.length);
                bufferPosition += bufferIncrement;
            }
        }
        frameQueue.publish(new Frame(buffer, generation.generationIndex));
    }
}
