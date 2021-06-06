package com.life.payload;

import java.util.Arrays;

public class Generation {

    public boolean[] generationArray;

    public Generation(boolean[] currentCells) {
        generationArray = new boolean[currentCells.length];
        System.arraycopy(currentCells, 0, generationArray, 0, currentCells.length);
    }

    @Override
    public boolean equals(Object o) {
        return Arrays.equals(generationArray, ((Generation) o).generationArray);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(generationArray);
    }
}
