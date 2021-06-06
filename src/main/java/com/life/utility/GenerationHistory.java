package com.life.utility;

import com.life.contract.Resettable;
import com.life.payload.Generation;
import com.life.payload.PackedGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GenerationHistory implements Resettable {

    private final GenerationCompressionBidiMap generationCompressionBidiMap;

    private final List<PackedGeneration> packedHistory;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GenerationHistory(GenerationCompressionBidiMap generationCompressionBidiMap) {
        this.generationCompressionBidiMap = generationCompressionBidiMap;
        this.packedHistory = new ArrayList<>();
    }

    public void add(Generation generation) {
        packedHistory.add(generationCompressionBidiMap.pack(generation));
    }

    public Optional<Integer> indexOf(Generation generation) {
        int index = packedHistory.indexOf(generationCompressionBidiMap.pack(generation));
        return (index == -1) ? Optional.empty() : Optional.of(index);
    }

    public Generation unpackFrameAtIndex(int index) {
        return generationCompressionBidiMap.unpack(packedHistory.get(index));
    }

    public int getLastIndex() {
        return packedHistory.size() - 1;
    }

    public int size() {
        return packedHistory.size();
    }

    @Override
    public void reset() {
        packedHistory.clear();
    }
}
