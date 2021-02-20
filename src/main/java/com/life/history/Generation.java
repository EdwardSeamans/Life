package com.life.history;

public class Generation {

    public final boolean[] generation;

    public Generation(boolean[] currentCells) {
        generation = new boolean[currentCells.length];
        System.arraycopy(currentCells, 0, generation, 0, currentCells.length);
    }
}
