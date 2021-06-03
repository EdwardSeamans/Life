package com.life.history;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class Generation {

    private static AtomicLong index = new AtomicLong(0L);

    public final long generationIndex;
    public final boolean[] generationArray;

    public Generation(boolean[] currentCells) {
        generationIndex = index.getAndIncrement();
        generationArray = new boolean[currentCells.length];
        System.arraycopy(currentCells, 0, generationArray, 0, currentCells.length);
    }

    @Override
    public boolean equals(Object o) {
        Generation that = (Generation) o;
        return Arrays.equals(generationArray, that.generationArray);
    }
}
