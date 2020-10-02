package com.life;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Random;

@Component
public class Life {

    private final Random random;

    private static final int COLUMNS = IterationSettings.COLUMNS;
    private static final int ROWS = IterationSettings.ROWS;
    private static final int BYTES_PER_PIXEL = IterationSettings.BYTES_PER_PIXEL;
    private static final int SCALING_FACTOR = IterationSettings.SCALING_FACTOR;
    private static final byte[] LIVE_COLOR = IterationSettings.GREEN;
    private static final byte[] DEAD_COLOR = IterationSettings.BLACK;


    private final int[] indicesNorth = new int[ROWS * COLUMNS];
    private final int[] indicesNorthEast = new int[ROWS * COLUMNS];
    private final int[] indicesEast = new int[ROWS * COLUMNS];
    private final int[] indicesSouthEast = new int[ROWS * COLUMNS];
    private final int[] indicesSouth = new int[ROWS * COLUMNS];
    private final int[] indicesSouthWest = new int[ROWS * COLUMNS];
    private final int[] indicesWest = new int[ROWS * COLUMNS];
    private final int[] indicesNorthWest = new int[ROWS * COLUMNS];

    private final byte[] LIVE_COLOR_CHUNK = new byte[BYTES_PER_PIXEL * SCALING_FACTOR];
    private final byte[] DEAD_COLOR_CHUNK = new byte[BYTES_PER_PIXEL * SCALING_FACTOR];

    private final boolean[] currentCells;
    private final boolean[] nextCells;
    private final boolean[] tempCells;

    private final byte[] currentBuffer;
    private final byte[] nextBuffer;
    private final byte[] tempBuffer;

    private final boolean[] deadCellStates;
    private final boolean[] liveCellStates;


    private final FrameQueue frameQueue;
    private final GenerationHistory generationHistory;

    private final static Logger LOG = LoggerFactory.getLogger(LifeSpringBootApplication.class);

    Life(FrameQueue frameQueue, GenerationHistory generationHistory) {
        this.frameQueue = frameQueue;
        this.generationHistory = generationHistory;
        this.random = new Random();
        this.currentCells = new boolean[ROWS * COLUMNS];
        this.nextCells = new boolean[ROWS * COLUMNS];
        this.tempCells = new boolean[ROWS * COLUMNS];
        this.currentBuffer = new byte[BYTES_PER_PIXEL * COLUMNS * SCALING_FACTOR * ROWS * SCALING_FACTOR];
        this.nextBuffer = new byte[BYTES_PER_PIXEL * COLUMNS * SCALING_FACTOR * ROWS * SCALING_FACTOR];
        this.tempBuffer = new byte[BYTES_PER_PIXEL * COLUMNS * SCALING_FACTOR * ROWS * SCALING_FACTOR];
        this.deadCellStates = new boolean[9];
        this.liveCellStates = new boolean[9];
        for (int scalingFactorRepitition = 0; scalingFactorRepitition < SCALING_FACTOR; scalingFactorRepitition++) {
            System.arraycopy(LIVE_COLOR, 0, LIVE_COLOR_CHUNK, scalingFactorRepitition * BYTES_PER_PIXEL, BYTES_PER_PIXEL);
            System.arraycopy(DEAD_COLOR, 0, DEAD_COLOR_CHUNK, scalingFactorRepitition * BYTES_PER_PIXEL, BYTES_PER_PIXEL);
        }
    }

    @PostConstruct
    public void initialize() {
        setRandomSeed();

        for (int index = 0; index < ROWS * COLUMNS; index++) {
            currentCells[index] = random.nextInt(100) < IterationSettings.INITIAL_POPULATION_PERCENT;
        }

        populateBufferFromCells(currentCells, currentBuffer);
        frameQueue.publishToQueue(currentBuffer);

        setRules();
        buildIndexTranslationArrays();
    }

    public void iterateCells() {
        for (int index = 0; index < ROWS * COLUMNS; index++) {
            int totalNeighbors = 0;

            if (currentCells[index + indicesNorth[index]]) {
                totalNeighbors++;
            }
            if (currentCells[index + indicesNorthEast[index]]) {
                totalNeighbors++;
            }
            if (currentCells[index + indicesEast[index]]) {
                totalNeighbors++;
            }
            if (currentCells[index + indicesSouthEast[index]]) {
                totalNeighbors++;
            }
            if (currentCells[index + indicesSouth[index]]) {
                totalNeighbors++;
            }
            if (currentCells[index + indicesSouthWest[index]]) {
                totalNeighbors++;
            }
            if (currentCells[index + indicesWest[index]]) {
                totalNeighbors++;
            }
            if (currentCells[index + indicesNorthWest[index]]) {
                totalNeighbors++;
            }

            if (currentCells[index]) {
                nextCells[index] = liveCellStates[totalNeighbors];
            } else {
                nextCells[index] = deadCellStates[totalNeighbors];
            }
        }
    }

    public void populateBufferFromCells(boolean [] cells, byte[] buffer) {
        byte[] cellColorChunk;
        byte[] rowChunk = new byte[COLUMNS * SCALING_FACTOR * BYTES_PER_PIXEL];
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
                System.arraycopy(rowChunk, 0, buffer, scalingRepetitions * rowIndex * BYTES_PER_PIXEL * SCALING_FACTOR * SCALING_FACTOR * COLUMNS , rowChunk.length);
            }
        }
    }

    private void cycleBuffers() {
        System.arraycopy(currentCells, 0, tempCells, 0, currentCells.length);
        System.arraycopy(nextCells, 0, currentCells, 0, nextCells.length);
        System.arraycopy(tempCells, 0, nextCells, 0, tempCells.length);
        System.arraycopy(currentBuffer, 0, tempBuffer, 0, currentBuffer.length);
        System.arraycopy(nextBuffer, 0, currentBuffer, 0, nextBuffer.length);
        System.arraycopy(tempBuffer, 0, nextBuffer, 0, tempBuffer.length);
    }

    @Scheduled(fixedDelay = IterationSettings.TARGET_FRAME_INTERVAL, initialDelay = 1000)
    public void letThereBeLight() {
        if (frameQueue.getBufferedFrameCount() > IterationSettings.TARGET_FRAME_RATE * 2) {
            LOG.info("FrameBuffer is full.");
            return;
        }
        iterateCells();
        populateBufferFromCells(nextCells, nextBuffer);
        cycleBuffers();
        frameQueue.publishToQueue(currentBuffer);
        //generationHistory.publishToQueue(currentCells);
    }

    public void setRandomSeed() {
        setSeed(random.nextLong());
        //setSeed(5787729581658046463L);
        //6639619142342545916 use with 80% for stable flower
        //-3640157045244410566
        //-3640157045244410566L crazy train 85% S23B36
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
        LOG.info("Seed for this run: " + seed);
    }

    public void setRules() {
        for (int index = 0; index < 9; index++) {
            deadCellStates[index] = false;
            liveCellStates[index] = false;
        }

        for (int neighborCount : IterationSettings.BIRTH) {
            deadCellStates[neighborCount] = true;
        }

        for (int neighborCount : IterationSettings.SURVIVE) {
            liveCellStates[neighborCount] = true;
        }
    }

    private void buildIndexTranslationArrays() {
        buildNorthIndices();
        buildEastIndices();
        buildSouthIndices();
        buildWestIndices();
        buildNorthEastIndices();
        buildSouthEastIndices();
        buildSouthWestIndices();
        buildNorthWestIndices();
    }

    private void buildNorthIndices() {
        Arrays.fill(indicesNorth, -COLUMNS);

        for (int i = 0; i < COLUMNS; i++) {
            indicesNorth[i] = (ROWS - 1) * COLUMNS;
        }
    }

    private void buildEastIndices() {
        Arrays.fill(indicesEast, 1);

        for (int i = COLUMNS - 1; i < ROWS * COLUMNS; i += COLUMNS) {
            indicesEast[i] = -COLUMNS + 1;
        }
    }

    private void buildSouthIndices() {
        Arrays.fill(indicesSouth, COLUMNS);

        for (int i = COLUMNS * (ROWS - 1); i < ROWS * COLUMNS; i ++) {
            indicesSouth[i] = -(COLUMNS * (ROWS - 1));
        }
    }

    private void buildWestIndices() {
        Arrays.fill(indicesWest, -1);

        for (int i = 0; i < ROWS * COLUMNS; i += COLUMNS) {
            indicesWest[i] = COLUMNS - 1;
        }
    }

    public void buildNorthEastIndices() {
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            indicesNorthEast[i] = indicesNorth[i];
            indicesNorthEast[i] += indicesEast[i];
        }
    }

    public void buildSouthEastIndices() {
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            indicesSouthEast[i] = indicesSouth[i];
            indicesSouthEast[i] += indicesEast[i];
        }
    }

    public void buildSouthWestIndices() {
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            indicesSouthWest[i] = indicesSouth[i];
            indicesSouthWest[i] += indicesWest[i];
        }
    }

    public void buildNorthWestIndices() {
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            indicesNorthWest[i] = indicesNorth[i];
            indicesNorthWest[i] += indicesWest[i];
        }
    }
}
