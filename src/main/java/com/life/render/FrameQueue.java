package com.life.render;

import com.life.configuration.IterationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class FrameQueue {
    private final ConcurrentLinkedQueue<ArrayList<Byte>> frameQueue;
    private final FrameTimer frameTimer;

    private int columns = IterationSettings.COLUMNS;

    private int rows = IterationSettings.ROWS;

    private int bytesPerPixel = IterationSettings.BYTES_PER_PIXEL;

    private byte[] lastReturnedFrame;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public FrameQueue(FrameTimer frameTimer) {
        LOG.info("Construction thread is " + Thread.currentThread().getName());
        this.frameQueue = new ConcurrentLinkedQueue<>();
        this.frameTimer = frameTimer;
        lastReturnedFrame = new byte[IterationSettings.SCALING_FACTOR * columns * IterationSettings.SCALING_FACTOR * rows * bytesPerPixel];
    }

    public void publishToQueue(byte[] frame) {
        ArrayList<Byte> frameList = new ArrayList<>();
        for (byte eightBits : frame) {
            frameList.add(eightBits);
        }
        frameQueue.add(frameList);
    }

    public byte[] getNextFrame() {
        byte[] workingArray = new byte[IterationSettings.SCALING_FACTOR * columns * IterationSettings.SCALING_FACTOR * rows * bytesPerPixel];
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
