package com.life.history;

import java.util.Arrays;

public class Generation {

    private final boolean[][] generation;

    public Generation(boolean[][] generation) {
        this.generation = generation;
    }

    @Override
    public boolean equals(Object o) {
        Generation that = (Generation) o;
        for (int row = 0; row < generation.length; row++) {
            for (int column = 0; column < generation[row].length; column++) {
                if (generation[row][column] != that.generation[row][column]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(generation);
    }
}
