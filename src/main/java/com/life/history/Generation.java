package com.life.history;

import java.util.Arrays;

public class Generation {

    private final boolean[] generation;

    public Generation(boolean[] currentCells) {
        generation = new boolean[currentCells.length];
        System.arraycopy(currentCells, 0, generation, 0, currentCells.length);
    }

    @Override
    public boolean equals(Object o) {
        Generation that = (Generation) o;
        return Arrays.equals(generation, that.generation);
    }
}
