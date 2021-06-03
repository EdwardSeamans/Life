package com.life.generate;

import com.life.configuration.IterationSettings;
import com.life.event.StartRunEvent;
import com.life.history.GenerationHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.life.configuration.IterationSettings.TARGET_FRAME_INTERVAL;
import static com.life.configuration.IterationSettings.TARGET_FRAME_RATE;

@Component
public class Life {

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
    private final boolean[] tempCells;

    private final boolean[] deadCellStates;
    private final boolean[] liveCellStates;

    private final AtomicBoolean wasCycleDiscovered;

    private final ApplicationContext context;
    private final GenerationQueue generationQueue;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    Life(GenerationQueue generationQueue, GenerationHistory generationHistory, ApplicationContext context) {
        this.wasCycleDiscovered = generationHistory.getWasCycleDiscovered();
        this.generationQueue = generationQueue;
        this.context = context;
        this.random = new Random();

        this.currentCells = new boolean[ROWS * COLUMNS];
        this.nextCells = new boolean[ROWS * COLUMNS];
        this.tempCells = new boolean[ROWS * COLUMNS];

        this.deadCellStates = new boolean[9];
        this.liveCellStates = new boolean[9];
    }

    @PostConstruct
    public void initialize() {
        setRandomSeed();
        setRules();
        buildIndexTranslationArrays();

        for (int index = 0; index < ROWS * COLUMNS; index++) {
            currentCells[index] = random.nextInt(100) < INITIAL_POPULATION_PERCENT;
        }

        generationQueue.publish(currentCells);
        letThereBeLight();
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

    private void cycleBuffers() {
        System.arraycopy(currentCells, 0, tempCells, 0, currentCells.length);
        System.arraycopy(nextCells, 0, currentCells, 0, nextCells.length);
        System.arraycopy(tempCells, 0, nextCells, 0, tempCells.length);
    }

    @Scheduled(fixedRate = 1, initialDelay = 0)
    public void letThereBeLight() {
        if(wasCycleDiscovered.get()) {
            return;
        }
            iterateCells(); //4.3ms
            cycleBuffers();
            generationQueue.publish(currentCells);
    }

    public void setRandomSeed() {
        //setSeed(random.nextLong());
        setSeed(-6632161742581681972L);
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
}
