package com.life.payload;

import java.util.Arrays;
import java.util.Objects;

public class PackedGeneration {
    public final byte[] packedArray;
    public final int generationCellSize;

    public PackedGeneration(byte[] packedArray, int generationCellSize) {
        this.packedArray = packedArray;
        this.generationCellSize = generationCellSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackedGeneration that = (PackedGeneration) o;
        return generationCellSize == that.generationCellSize && Arrays.equals(packedArray, that.packedArray);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(generationCellSize);
        result = 31 * result + Arrays.hashCode(packedArray);
        return result;
    }
}
