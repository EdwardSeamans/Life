package com.life.utility;

import com.life.payload.Generation;
import com.life.payload.PackedGeneration;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class GenerationCompressionBidiMap {

    private final BidiMap<ByteChunk, BooleanArrayChunk> compressionMap;

    private static final int MAX_UNSIGNED_BYTE_VALUE = 255;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GenerationCompressionBidiMap() {
        this.compressionMap = buildCompressionMap();
    }

    private static BidiMap<ByteChunk, BooleanArrayChunk> buildCompressionMap() {
        LOG.info("Building compression map.");
        BidiMap<ByteChunk, BooleanArrayChunk> compressionMap = new DualHashBidiMap<>();
        for (int index = 0; index <= MAX_UNSIGNED_BYTE_VALUE; index++) {
            ByteChunk byteChunk = new ByteChunk((byte)(index - 128));
            compressionMap.put(byteChunk, byteChunk.toBooleanArrayChunk());
        }
        LOG.info("Compression map completed.");
        return compressionMap;
    }

    public PackedGeneration pack(Generation generation) {
        int generationCellSize = generation.generationArray.length;
        int fullBytes = generationCellSize / 8;
        int bitsInLastByte = generationCellSize % 8;
        byte[] packedArray;
        if (bitsInLastByte == 0) {
            packedArray = new byte[fullBytes];
        } else {
            packedArray = new byte[fullBytes + 1];
        }
        int packedArrayIndex = 0;
        BooleanArrayChunk booleanArrayChunk = new BooleanArrayChunk(new boolean[] {false, false, false, false, false, false, false, false});
        for (int generationIndex = 0; generationIndex < generationCellSize; generationIndex += 8) {
            System.arraycopy(generation.generationArray, generationIndex, booleanArrayChunk.value, 0, 8);
            packedArray[packedArrayIndex] = compressionMap.getKey(booleanArrayChunk).value;
            packedArrayIndex++;
        }
        if (bitsInLastByte != 0) {
            int generationArrayPartialByteStartIndex = fullBytes * 8;
            boolean[] paddedBooleanArray = {false, false, false, false, false, false, false, false};
            System.arraycopy(generation.generationArray, generationArrayPartialByteStartIndex, paddedBooleanArray, 0, bitsInLastByte);
            packedArray[packedArrayIndex] = compressionMap.getKey(paddedBooleanArray).value;
        }
        return new PackedGeneration(packedArray, generationCellSize);
    }

    public Generation unpack(PackedGeneration packedGeneration) {
        int generationCellSize = packedGeneration.generationCellSize;
        boolean[] generationArray = new boolean[packedGeneration.generationCellSize];
        int fullBytes = generationCellSize / 8;
        int bitsInLastByte = generationCellSize % 8;
        int byteChunkArrayIndex = 0;
        ByteChunk byteChunk = new ByteChunk(packedGeneration.packedArray[byteChunkArrayIndex]);
        for (int generationArrayByteStartIndex = 0; generationArrayByteStartIndex < fullBytes * 8; generationArrayByteStartIndex += 8) {
            System.arraycopy(compressionMap.get(byteChunk).value, 0, generationArray, generationArrayByteStartIndex, 8);
            byteChunkArrayIndex++;
            if (byteChunkArrayIndex < packedGeneration.packedArray.length) {
                byteChunk.value = packedGeneration.packedArray[byteChunkArrayIndex];
            }
        }
        System.arraycopy(compressionMap.get(byteChunk).value, 0, generationArray, fullBytes * 8, bitsInLastByte);

        return new Generation(generationArray);
    }
}
