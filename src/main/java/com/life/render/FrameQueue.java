package com.life.render;

import com.life.configuration.IterationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class FrameQueue {
    private final ConcurrentLinkedQueue<ArrayList<Byte>> frameQueue;
    private final FrameTimer frameTimer;

    private static final int COLUMNS = IterationSettings.COLUMNS;

    private static final int ROWS = IterationSettings.ROWS;

    private static final int BYTES_PER_PIXEL = IterationSettings.BYTES_PER_PIXEL;

    private byte[] lastReturnedFrame;

    private static final Logger LOG = LoggerFactory.getLogger(FrameQueue.class);

    public FrameQueue(FrameTimer frameTimer) {
        LOG.info("Construction thread is " + Thread.currentThread().getName());
        this.frameQueue = new ConcurrentLinkedQueue<>();
        this.frameTimer = frameTimer;
        lastReturnedFrame = new byte[IterationSettings.SCALING_FACTOR * COLUMNS * IterationSettings.SCALING_FACTOR * ROWS * BYTES_PER_PIXEL];
    }

    public void publishToQueue(byte[] frame) {
        ArrayList<Byte> frameList = new ArrayList<>();
        for (byte eightBits : frame) {
            frameList.add(eightBits);
        }
        frameQueue.add(frameList);
    }

    public byte[] getNextFrame() {
        byte[] workingArray = new byte[IterationSettings.SCALING_FACTOR * COLUMNS * IterationSettings.SCALING_FACTOR * ROWS * BYTES_PER_PIXEL];
        if (frameQueue.isEmpty()) {
            return lastReturnedFrame;
        }
        List<Byte> workingList = frameQueue.remove();
        for (int index = 0; index < workingArray.length; index++) {
            workingArray[index] = workingList.get(index);
        }
        lastReturnedFrame = workingArray;
        frameTimer.tick();
        return workingArray;
    }

    public int getBufferedFrameCount() {
        return frameQueue.size();
    }
}
