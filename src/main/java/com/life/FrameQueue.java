package com.life;

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

    private static final int COLUMNS = GlobalSettings.COLUMNS;

    private static final int ROWS = GlobalSettings.ROWS;

    private static final int BYTES_PER_PIXEL = GlobalSettings.BYTES_PER_PIXEL;

    private byte[] lastReturnedFrame;

    private static final Logger LOG = LoggerFactory.getLogger(FrameQueue.class);

    public FrameQueue(FrameTimer frameTimer) {
        this.frameQueue = new ConcurrentLinkedQueue<>();
        this.frameTimer = frameTimer;
        lastReturnedFrame = new byte[2 * COLUMNS * 2 * ROWS * BYTES_PER_PIXEL];
    }

    public void publishToQueue(byte[] frame) {
        ArrayList<Byte> frameList = new ArrayList<>();
        for (byte eightBits : frame) {
            frameList.add(eightBits);
        }
        frameQueue.add(frameList);
    }

    public byte[] getNextFrame() {
        byte[] workingArray = new byte[2 * COLUMNS * 2 * ROWS * BYTES_PER_PIXEL];
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
