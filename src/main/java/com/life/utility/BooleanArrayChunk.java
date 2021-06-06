package com.life.utility;

import java.util.Arrays;

public class BooleanArrayChunk {
    public boolean[] value;

    public BooleanArrayChunk(boolean[] value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanArrayChunk that = (BooleanArrayChunk) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return "BooleanArrayChunk {" +
                " value = " + Arrays.toString(value) +
                " }";
    }
}
