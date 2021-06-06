package com.life.utility;

public class ByteChunk {
    public byte value;

    public ByteChunk(byte value) {
        this.value = value;
    }

    public BooleanArrayChunk toBooleanArrayChunk() {
        boolean[] booleanArray = new boolean[8];
        int index = 7;
        for (int intValue = value + 128; index >= 0; intValue /= 2) {
            booleanArray[index] = intValue % 2 == 1;
            index--;
        }
        return new BooleanArrayChunk(booleanArray);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteChunk byteChunk = (ByteChunk) o;
        return value == byteChunk.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return "ByteChunk { " +
                "value = " + value +
                " }";
    }
}
