package com.life.generate;

import com.life.history.Generation;
import com.life.history.GenerationHistory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class GenerationQueue {

    private final GenerationHistory generationHistory;

    private final ConcurrentLinkedQueue<Generation> generationQueue;

    public GenerationQueue(GenerationHistory generationHistory) {
        this.generationHistory = generationHistory;
        this.generationQueue = new ConcurrentLinkedQueue<>();
    }

    public void publish(boolean[] generationArray) {
        Generation generation = new Generation(generationArray);
        generationQueue.add(generation);
        generationHistory.publish(generation);
    }

    public Generation getNextGeneration() {
        return generationQueue.remove();
    }

    public boolean isEmpty() {
        return generationQueue.isEmpty();
    }
}