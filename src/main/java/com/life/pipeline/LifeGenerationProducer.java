package com.life.pipeline;

import com.life.configuration.IterationSettings;
import com.life.contract.Pipeline;
import com.life.executor.PipelineExecutor;
import com.life.payload.Generation;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class LifeGenerationProducer implements Pipeline {

    private final Random random;

    private static final int COLUMNS = IterationSettings.COLUMNS;
    private static final int ROWS = IterationSettings.ROWS;
    private static final int INITIAL_POPULATION_PERCENT = IterationSettings.INITIAL_POPULATION_PERCENT;
    private static final int[] BIRTH = IterationSettings.BIRTH;
    private static final int[] SURVIVE = IterationSettings.SURVIVE;

    private final int[] indicesNorth = new int[ROWS * COLUMNS];
    private final int[] indicesNorthEast = new int[ROWS * COLUMNS];
    private final int[] indicesEast = new int[ROWS * COLUMNS];
    private final int[] indicesSouthEast = new int[ROWS * COLUMNS];
    private final int[] indicesSouth = new int[ROWS * COLUMNS];
    private final int[] indicesSouthWest = new int[ROWS * COLUMNS];
    private final int[] indicesWest = new int[ROWS * COLUMNS];
    private final int[] indicesNorthWest = new int[ROWS * COLUMNS];

    private final boolean[] currentCells;
    private final boolean[] nextCells;

    private final boolean[] deadCellStates;
    private final boolean[] liveCellStates;

    private final Runnable action;
    private final AtomicBoolean isPaused;
    private final StringProperty actionNameProperty;

    private final GenerationProcessingQueue generationProcessingQueue;

    private static final String ACTION_NAME_PROPERTY_STRING = "Produce Generations";
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    LifeGenerationProducer(GenerationProcessingQueue generationProcessingQueue, PipelineExecutor pipelineExecutor) {

        this.generationProcessingQueue = generationProcessingQueue;
        this.random = new Random();

        this.currentCells = new boolean[ROWS * COLUMNS];
        this.nextCells = new boolean[ROWS * COLUMNS];

        this.deadCellStates = new boolean[9];
        this.liveCellStates = new boolean[9];

        buildIndexTranslationArrays();

        this.action = this::produceLifeGenerations;
        this.actionNameProperty = new SimpleStringProperty(ACTION_NAME_PROPERTY_STRING);
        this.isPaused = pipelineExecutor.isPaused();
        pipelineExecutor.registerAndRun(this);
    }

    public void produceLifeGenerations() {
        resetSimulation();
        while (true) {
            if (isPaused.get()) {
                continue;
            }
            iterateCells();
            System.arraycopy(nextCells, 0, currentCells, 0, nextCells.length);
            try {
                generationProcessingQueue.put(new Generation(currentCells));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetSimulation() {
        setRandomSeed();
        setRules();
        for (int index = 0; index < ROWS * COLUMNS; index++) {
            currentCells[index] = random.nextInt(100) < INITIAL_POPULATION_PERCENT;
        }
        try {
            generationProcessingQueue.put(new Generation(currentCells));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public void setRandomSeed() {
        setSeed(random.nextLong());
        //random.nextLong()
        //-7374350501714555895 1000 X 500 X 2 40%
        //setSeed(1128755554682849221L);
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

        for (int neighborCount : BIRTH) {
            deadCellStates[neighborCount] = true;
        }

        for (int neighborCount : SURVIVE) {
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

    @Override
    public Runnable getTask() {
        return action;
    }

    @Override
    public StringProperty actionNameProperty() {
        return actionNameProperty;
    }
}
