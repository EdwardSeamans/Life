package com.life;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class Life {

    private final Random random;

    public static final int COLUMNS = GlobalSettings.COLUMNS;
    public static final int ROWS = GlobalSettings.ROWS;
    private static final int BYTES_PER_PIXEL = GlobalSettings.BYTES_PER_PIXEL;

    private final boolean[][] currentCells;
    private final boolean[][] nextCells;

    private byte[] currentBuffer;
    private byte[] nextBuffer;

    private final boolean[] deadCellStates;
    private final boolean[] liveCellStates;

    private final FrameQueue frameQueue;
    private final GenerationHistory generationHistory;

    private final static Logger LOG = LoggerFactory.getLogger(LifeSpringBootApplication.class);

    Life(FrameQueue frameQueue, GenerationHistory generationHistory) {
        this.frameQueue = frameQueue;
        this.generationHistory = generationHistory;
        this.random = new Random();
        this.currentCells = new boolean[ROWS][COLUMNS];
        this.nextCells = new boolean[ROWS][COLUMNS];
        this.currentBuffer = new byte[BYTES_PER_PIXEL * COLUMNS * 2 * ROWS * 2];
        this.nextBuffer = new byte[BYTES_PER_PIXEL * COLUMNS * 2 * ROWS * 2];
        this.deadCellStates = new boolean[9];
        this.liveCellStates = new boolean[9];
    }

    @PostConstruct
    public void initialize() {
        setRandomSeed();
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                currentCells[row][column] = random.nextInt(100) < GlobalSettings.INITIAL_POPULATION_PERCENT;
            }
        }
        populateBufferFromCells(currentCells, currentBuffer);
        frameQueue.publishToQueue(currentBuffer);

        setRules();
    }

    public void iterateCells() {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                evaluateCell(row, column);
            }
        }
    }

    private void evaluateCell(int row, int column) {
        int totalNeighbors = countNeighbors(row, column);

        if (currentCells[row][column]) {
            nextCells[row][column] = liveCellStates[totalNeighbors];
            return;
        } else {
            nextCells[row][column] = deadCellStates[totalNeighbors];
        }
    }

    private int countNeighbors(int row, int column) {
        int count = 0;
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                if (rowOffset == 0 && columnOffset == 0) {
                    continue;
                }
                if (isOccupied(row + rowOffset, column + columnOffset)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isOccupied(int row, int column) {
        int translatedRow = row;
        int translatedColumn = column;

        int lastRowIndex = ROWS - 1;
        int lastColumnIndex = COLUMNS - 1;

        if (row < 0) {
            translatedRow = lastRowIndex;
        }

        if (row > lastRowIndex) {
            translatedRow = 0;
        }

        if (column < 0) {
            translatedColumn = lastColumnIndex;
        }

        if (column > lastRowIndex) {
            translatedColumn = 0;
        }

        return currentCells[translatedRow][translatedColumn];
    }

    public void populateBufferFromCells(boolean[][] cells, byte[] buffer) {
        int currentPixelGreenIndex = 1;
        for (int row = 0; row < ROWS; row++) {
            for (int repetition = 1; repetition < 3; repetition++) {
                for (int column = 0; column < COLUMNS; column++) {
                    if (cells[row][column]) {
                        buffer[currentPixelGreenIndex] = -1;
                        buffer[currentPixelGreenIndex + BYTES_PER_PIXEL] = -1;
                    } else {
                        buffer[currentPixelGreenIndex] = 0;
                        buffer[currentPixelGreenIndex + BYTES_PER_PIXEL] = 0;
                    }
                    currentPixelGreenIndex += BYTES_PER_PIXEL * 2;
                }
            }
        }
    }

    private void cycleBuffers() {
        for (int column = 0; column < COLUMNS; column++) {
            for (int row = 0; row < ROWS; row++) {
                currentCells[row][column] = nextCells[row][column];
            }
        }
        byte[] temp = currentBuffer;
        currentBuffer = nextBuffer;
        nextBuffer = temp;
    }

    @Scheduled(fixedDelay = GlobalSettings.TARGET_FRAME_INTERVAL, initialDelay = 1000)
    public void letThereBeLight() {
        if (frameQueue.getBufferedFrameCount() > GlobalSettings.TARGET_FRAME_RATE * 2) {
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

        for (int neighborCount : GlobalSettings.BIRTH) {
            deadCellStates[neighborCount] = true;
        }

        for (int neighborCount : GlobalSettings.SURVIVE) {
            liveCellStates[neighborCount] = true;
        }
    }
}
